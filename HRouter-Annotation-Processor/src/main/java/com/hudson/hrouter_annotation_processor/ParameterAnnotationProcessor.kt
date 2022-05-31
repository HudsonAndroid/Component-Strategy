package com.hudson.hrouter_annotation_processor

import com.google.auto.service.AutoService
import com.hudson.hrouter.annotation.HRouter
import com.hudson.hrouter.annotation.Parameter
import com.hudson.hrouter_annotation_processor.param.DIMethod
import com.hudson.hrouter_annotation_processor.utils.isValidRoutePageType
import com.squareup.javapoet.*
import java.io.IOException
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

/**
 * 路由参数注解处理器
 *
 * 将所有被Parameter注解的信息收集起来，
 * 归纳到一个key:Class, value:List<Field>的集合中
 * Created by Hudson on 2022/5/31.
 */
@AutoService(Processor::class)
@SupportedAnnotationTypes("com.hudson.hrouter.annotation.Parameter") // 需要处理的注解类
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class ParameterAnnotationProcessor: AbstractProcessor() {
    // 操作Element（方法，类，属性都是Element）的工具类
    private var elementsUtils: Elements? = null

    // 处理类信息的工具类
    private var typeUtils: Types? = null

    // 编译器日志打印类
    private var messager: Messager? = null

    // java文件生成工具
    private var filer: Filer? = null

    // 收集的集合
    private val paramFieldMap = mutableMapOf<TypeElement, MutableList<Element>>()

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        elementsUtils = processingEnv?.elementUtils
        typeUtils = processingEnv?.typeUtils
        messager = processingEnv?.messager
        filer = processingEnv?.filer
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        val elements = roundEnv?.getElementsAnnotatedWith(Parameter::class.java)
        if(elements?.isNotEmpty() == true){
            for(element in elements){
                if(element is VariableElement){
                    collectFieldInfo(element)
                }else{
                    messager?.printMessage(Diagnostic.Kind.WARNING,
                        "Parameter注解只能在字段上使用")
                }
            }
            generateDICodeFile()
            return true
        }
        return false
    }

    /**
     * 收集字段上的信息
     */
    private fun collectFieldInfo(fieldElement: Element) {
        // 由于拿到的是字段，我们首先要拿到字段所属的类信息 (TypeElement代表类类型)
        val typeElement = fieldElement.enclosingElement as? TypeElement
        typeElement?.apply {
            if (paramFieldMap.containsKey(this)) {
                paramFieldMap[this]?.add(fieldElement)
            } else {
                val fieldList = mutableListOf<Element>()
                fieldList.add(fieldElement)
                paramFieldMap[this] = fieldList
            }
        }
    }

    /**
     * 生成依赖注入的注入类文件
     *
     * 一个key对应一个注入类文件，每个注入类继承自[ParameterInjector]，
     * 该类位于HRouter-Api中
     *
     * 文件大致像
     *
     *  public class OrderMainActivity$$ParameterInjector implements ParameterInjector {
            @Override
            public void inject(Object targetObject) {
                OrderMainActivity t = (OrderMainActivity)targetObject;

                t.name = t.getIntent().getStringExtra("name");
                t.age = t.getIntent().getIntExtra("age");
                ....
            }
        }
     */
    private fun generateDICodeFile(){
        if(paramFieldMap.isNotEmpty()){
            paramFieldMap.entries.forEach {
                val waitInjectClazzType = it.key
                waitInjectClazzType.isValidRoutePageType(typeUtils, elementsUtils)
                    ?: throw IllegalArgumentException("不支持的路由页面类型：${waitInjectClazzType.asType()}")

                val diMethodBuilder = DIMethod.Builder()
                    .diTypeElement(waitInjectClazzType)
                    .fieldElements(it.value)
                    .messager(messager)

                val fileClassName = "${waitInjectClazzType.simpleName}_ParameterInjector"
                val pkgName = ClassName.get(waitInjectClazzType).packageName()
                // 生成类文件
                messager?.printMessage(Diagnostic.Kind.NOTE, "路由注解生成文件:$pkgName.$fileClassName")

                val superInterfaceType = elementsUtils?.getTypeElement("com.hudson.hrouter_api.param.ParameterInjector")
                val clazzInfo = TypeSpec.classBuilder(fileClassName)
                    .addSuperinterface(ClassName.get(superInterfaceType!!))
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(diMethodBuilder.build())
                    .build()

                try {
                    JavaFile.builder(
                        pkgName,
                        clazzInfo
                    ).build().writeTo(filer)
                }catch (e: IOException){
                    e.printStackTrace()
                }
            }
        }
    }

}