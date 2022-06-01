package com.hudson.hrouter_annotation_processor

import com.google.auto.service.AutoService
import com.hudson.hrouter.annotation.HRouter
import com.hudson.hrouter.annotation.bean.RouteInfo
import com.hudson.hrouter.annotation.enums.PageType
import com.hudson.hrouter_annotation_processor.HRouterAnnotationProcessor.Companion.KEY_GEN_ROUTER_PKG
import com.hudson.hrouter_annotation_processor.HRouterAnnotationProcessor.Companion.KEY_COMPONENT_NAME
import com.hudson.hrouter_annotation_processor.repository.RepositoryFileGenerator
import com.hudson.hrouter_annotation_processor.utils.isValidRoutePageType
import java.lang.IllegalArgumentException
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic


/**
 * HRouter的注解处理器
 */
@AutoService(Processor::class) // 帮助我们注册注解处理器
@SupportedAnnotationTypes("com.hudson.hrouter.annotation.HRouter") // 需要处理的注解类
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(
    KEY_GEN_ROUTER_PKG,
    KEY_COMPONENT_NAME
)
class HRouterAnnotationProcessor: AbstractProcessor() {
    companion object{
        const val KEY_COMPONENT_NAME = "ModuleName"

        const val KEY_GEN_ROUTER_PKG = "RouterPkg"
    }

    // 操作Element（方法，类，属性都是Element）的工具类
    private var elementsUtils: Elements? = null

    // 处理类信息的工具类
    private var typeUtils: Types? = null

    // 编译器日志打印类
    private var messager: Messager? = null

    // java文件生成工具
    private var filer: Filer? = null

    // 组件名
    private var componentName: String? = null

    // 产生文件存放的包名
    private var routePkg: String? = null

    // 所有的路由，key:group， value:路由集合
    private val allRoutes: MutableMap<String, MutableList<RouteInfo>>  = mutableMapOf()


    // 初始化工作
    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        elementsUtils = processingEnv?.elementUtils
        typeUtils = processingEnv?.typeUtils
        messager = processingEnv?.messager
        filer = processingEnv?.filer

        // 获取外界配置的参数信息
        processingEnv?.options?.apply {
            componentName = get(KEY_COMPONENT_NAME)
            routePkg = get(KEY_GEN_ROUTER_PKG)
        }

        // 检查参数信息
        checkRouterParams()
    }

    private fun checkRouterParams() {
        if (componentName.isNullOrEmpty() || routePkg.isNullOrEmpty()) {
            throwException("路由信息配置错误，groupName=$componentName, routePackageName=$routePkg")
        }
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        if(annotations.isNullOrEmpty()){
//            messager?.printMessage(Diagnostic.Kind.NOTE, "没有找到HRouter注解的类")
            return false
        }
        // 拿到所有被HRouter注解的类信息
        val elements = roundEnv?.getElementsAnnotatedWith(HRouter::class.java) ?: emptySet()
        for(element in elements){
            // 拿到注解，从注解中解析参数
            val routerAnnotation = element.getAnnotation(HRouter::class.java)

            createRouteInfoByAnnotation(element, routerAnnotation)?.let {
                val routeList = allRoutes[it.group] ?: mutableListOf()
                routeList.add(it)
                allRoutes[it.group] = routeList
            }
        }
        RepositoryFileGenerator.Builder(
                allRoutes,
                elementsUtils,
                filer,
                componentName!!)
            .generatePkg(routePkg)
            .messager(messager)
            .build()
            .generateFiles()

        return true
    }

    /**
     * 通过注解解析路由信息
     */
    private fun createRouteInfoByAnnotation(element: Element, routerAnnotation: HRouter): RouteInfo? {
        // 获取到被注解的类的信息
        val matchedPageType: PageType? = element.isValidRoutePageType(typeUtils, elementsUtils)
        if(matchedPageType == null){
            messager?.printMessage(Diagnostic.Kind.WARNING,
                "HRouter注解的页面类型没有可匹配的路由类型，type:${element.asType()}, path=${routerAnnotation.path}, group=${routerAnnotation.group}")
            return null
        }
        val routeInfo = RouteInfo(
            matchedPageType,
            routerAnnotation.path,
            routerAnnotation.group
        ).apply {
            pageElement = element as TypeElement
        }
        checkRouteConfig(routeInfo)
        return routeInfo
    }

    /**
     * 检查路由配置信息是否合法
     */
    private fun checkRouteConfig(route: RouteInfo) {
        val path = route.path
        val group = route.group
        val isPathValid = path.isNotEmpty() && // 不为空
                path.startsWith("/") &&  // 以/开头
                path.lastIndexOf("/") != 0  // 不是 /
        if(!isPathValid){
            throwException("路由路径配置错误，path=$path")
        }

        // 检查group
        val groupNameFromPath = path.substring(1,path.indexOf("/",1))

        if(group.isEmpty()){
            // 没有配置group信息
            route.group = groupNameFromPath
        }else{
            // 有配置group信息
            if(groupNameFromPath != group){
                messager?.printMessage(Diagnostic.Kind.WARNING,
                    "${route.pageElement?.asType()}配置group和path中的group不匹配, path=$path, group=$group, 组件：$componentName")
            }
        }
    }

    private fun throwException(errMsg: String){
        if (messager != null) {
            messager?.printMessage(
                Diagnostic.Kind.ERROR,
                errMsg
            )
        } else {
            throw IllegalArgumentException(errMsg)
        }
    }
}