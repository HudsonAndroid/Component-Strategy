package com.hudson.hrouter_annotation_processor.repository

import com.hudson.hrouter.annotation.bean.RouteInfo
import com.hudson.hrouter.annotation.enums.PageType
import com.squareup.javapoet.*
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.lang.model.element.Modifier
import javax.lang.model.util.Elements
import javax.tools.Diagnostic


/**
 * 路由仓库类生成器
 * Created by Hudson on 2022/5/31.
 */
class RepositoryFileGenerator(
    builder: Builder
) {
    private val routeMap = builder.routeMap
    private val elementUtils = builder.elementUtils
    private val filer = builder.filer
    private val messager = builder.messager
    private val generatePkg = builder.generatePkg
    private val componentName = builder.componentName

    private val groupMap: MutableMap<String, String> = mutableMapOf()

    companion object{
        private const val ComponentGroupRepoFilePrefix = "HRouterComponent"
    }

    fun generateFiles(){
        if(routeMap.isNotEmpty()){
            generatePathRepositoryFile()
            generateGroupRepositoryFile()
        }
    }

    /**
     * 生成单个Group的映射表类，一个组对应一个PathRepository
     *
     * public class HRouter_{Group}_Path_Repo implements GroupPathRepository {

        @Override
        public Map<String, RouteInfo> getPathRepository() {
            Map<String, RouteInfo> pathMap = new HashMap<>();

            pathMap.put("/personal/PersonalMainActivity",
                RouterInfo(
                    PageType.ACTIVITY,
                    RouterBean.TypeEnum.ACTIVITY,
                    "/personal/PersonalMainActivity",
                    "personal"
                )
            );
            return pathMap;
        }
    }
     */
    private fun generatePathRepositoryFile(){
        // 返回值 Map<String, RouteInfo>
        val methodReturn = ParameterizedTypeName.get(
            ClassName.get(MutableMap::class.java),
            ClassName.get(String::class.java),
            ClassName.get(RouteInfo::class.java)
        )

        // 一个Entry对应生成一个Group，也对应一个PathRepository
        for(entry in routeMap.entries){
            val methodSpecBuilder: MethodSpec.Builder =
                MethodSpec.methodBuilder("getPathRepository")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(methodReturn)
            // Map<String, RouterBean> pathMap = new HashMap<>();
            methodSpecBuilder.addStatement(
                "\$T<\$T,\$T> \$N = new \$T<>()",
                ClassName.get(MutableMap::class.java),
                ClassName.get(String::class.java),
                ClassName.get(RouteInfo::class.java),
                "pathMap",
                ClassName.get(HashMap::class.java)
            )

            for(routeInfo in entry.value){
                insertRouteInfo(methodSpecBuilder, routeInfo)
            }

            // return pathMap;
            methodSpecBuilder.addStatement("return \$N", "pathMap")

            // HRouter_{Group}_Path_Repo
            val group = entry.key
            val finalClassName = "HRouter_${group}_Path_Repo"

            messager?.printMessage(
                Diagnostic.Kind.NOTE,
                "HRouter: 组件=${componentName}, 路由组=$group， Group路由表类=$generatePkg.$finalClassName"
            )

            val superInterfaceType = elementUtils?.getTypeElement("com.hudson.hrouter_api.GroupPathRepository")
            JavaFile.builder(
                generatePkg,
                TypeSpec.classBuilder(finalClassName)
                    .addSuperinterface(ClassName.get(superInterfaceType))
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(methodSpecBuilder.build())
                    .build())
                .build()
                .writeTo(filer)
            groupMap[group] = finalClassName
        }
    }

    /**
     * pathMap.put("/personal/PersonalMainActivity",
        RouterInfo(
            PageType.ACTIVITY,
            RouterBean.TypeEnum.ACTIVITY,
            "/personal/PersonalMainActivity",
            "personal"
          )
       );
     */
    private fun insertRouteInfo(builder: MethodSpec.Builder, routeInfo: RouteInfo){
        builder.addStatement(
            "\$N.put(\$S, new \$T(\$T.\$L, \$S, \$S, \$T.class))",
            "pathMap",
            routeInfo.path,
            ClassName.get(RouteInfo::class.java),
            ClassName.get(PageType::class.java),
            routeInfo.pageType,
            routeInfo.path,
            routeInfo.group,
            ClassName.get(routeInfo.pageElement)
        )
    }

    /**
     * 生成组件路由Group表类
     * public class ARouter$$Group$$personal implements ComponentGroupRepository {

            @Override
            public Map<String, Class<? extends GroupPathRepository>> getGroupRepository() {
                Map<String, Class<? extends GroupPathRepository>> groupMap = new HashMap<>();
                groupMap.put("personal", HRouter_personal_Path_Repo.class);
                return groupMap;
            }
        }
     */
    private fun generateGroupRepositoryFile(){
        if (routeMap.isEmpty() || groupMap.isEmpty()) {
            return
        }

        // 方法返回值
        val thirdParam = ParameterizedTypeName.get(
            ClassName.get(Class::class.java),
            WildcardTypeName.subtypeOf(
                ClassName.get(
                    elementUtils?.getTypeElement("com.hudson.hrouter_api.GroupPathRepository")
                )
            )
        )
        val methodReturn = ParameterizedTypeName.get(
            ClassName.get(MutableMap::class.java),
            ClassName.get(String::class.java),
            thirdParam
        )

        // Method
        val methodBuilder = MethodSpec.methodBuilder("getGroupRepository")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .returns(methodReturn)

        // 方法体
        /*
         * Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
         */
        methodBuilder.addStatement(
            "\$T<\$T, \$T> \$N = new \$T<>()",
            ClassName.get(MutableMap::class.java),
            ClassName.get(String::class.java),
            thirdParam,
            "groupMap",
            ClassName.get(java.util.HashMap::class.java)
        )

        // 遍历整个组件中所有的group，按顺序增加put逻辑
        /*
         * groupMap.put("personal", HRouter_personal_Path_Repo.class);
         */
        val entries: Set<Map.Entry<String, String>> = groupMap.entries
        for ((key, value) in entries) {
            methodBuilder.addStatement(
                "\$N.put(\$S, \$T.class)",
                "groupMap",
                key,
                ClassName.get(generatePkg, value)
            )
        }

        // 返回行
        methodBuilder.addStatement("return \$N", "groupMap")

        // 构建类信息
        val finalClassName = "$ComponentGroupRepoFilePrefix$componentName"
        messager?.printMessage(
            Diagnostic.Kind.NOTE,
            "HRouter: 组件=${componentName}, 组件路由表类=$generatePkg.$finalClassName"
        )

        JavaFile.builder(
            generatePkg,
            TypeSpec.classBuilder(finalClassName)
                .addSuperinterface(
                    ClassName.get(
                        elementUtils?.getTypeElement("com.hudson.hrouter_api.ComponentGroupRepository")
                    )
                )
                .addModifiers(Modifier.PUBLIC)
                .addMethod(methodBuilder.build())
                .build()
            )
            .build()
            .writeTo(filer)
    }

    /**
     * 构造器
     *
     * @param routeMap group-list<route>键值对
     * @param elementUtils element操作工具
     * @param filer 用于生成java文件
     * @param componentName 用于生成目标Group仓库文件名的拼接
     */
    class Builder(
        val routeMap: Map<String, MutableList<RouteInfo>>,
        val elementUtils: Elements?,
        val filer: Filer?,
        val componentName: String
    ){

        var messager: Messager? = null
            private set

        var generatePkg: String = "com.hudson.hrouter.generate"

        var pathRepoFileNameRule: (group: String)->String = {
            "HRouter_${it}_Path_Repo"
        }

        /**
         * 日志打印工具
         */
        fun messager(messager: Messager?) = apply {
            this.messager = messager
        }

        /**
         * 生成的代码文件的包名
         */
        fun generatePkg(generatePkg: String?) = apply {
            if(generatePkg.isNullOrEmpty()) return@apply
            this.generatePkg = generatePkg
        }

        /**
         * 生成的单个组路由仓库的文件名规则
         *
         * {group} 当前的路由组
         */
        fun pathRepoFileNameRule(rule: (group: String) -> String) {
            this.pathRepoFileNameRule = rule
        }

        fun build() = RepositoryFileGenerator(this)
    }
}