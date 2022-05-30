package com.hudson.hrouter_annotation_processor

import com.google.auto.service.AutoService
import com.hudson.hrouter.annotation.HRouter
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import java.io.IOException
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Modifier
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
class HRouterAnnotationProcessor: AbstractProcessor() {
    // 操作Element（方法，类，属性都是Element）的工具类
    private var elementsUtils: Elements? = null

    // 处理类信息的工具类
    private var typeUtils: Types? = null

    // 编译器日志打印类
    private var messager: Messager? = null

    // java文件生成工具
    private var filer: Filer? = null

    // 初始化工作
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
        System.out.println("输出===========")
        messager?.printMessage(Diagnostic.Kind.NOTE, "开始处理了")
        if(annotations.isNullOrEmpty()){
            messager?.printMessage(Diagnostic.Kind.NOTE, "没有找到HRouter注解的类")
            return false
        }
        // 拿到所有被HRouter注解的类信息
        val elements = roundEnv?.getElementsAnnotatedWith(HRouter::class.java) ?: emptySet()
        for(element in elements){
            val packageInfo = elementsUtils?.getPackageOf(element)
            val packageName = packageInfo?.simpleName.toString()

            messager?.printMessage(Diagnostic.Kind.NOTE, "包名信息$packageName")

            // 获取类名
            val className = element.simpleName.toString()
            messager?.printMessage(Diagnostic.Kind.NOTE, "类名$className")

            // 拿到注解，从注解中解析参数
            val routerAnnotation = element.getAnnotation(HRouter::class.java)

            generateRouteInfo()
        }

        return false
    }

    private fun generateRouteInfo(){
        val main = MethodSpec.methodBuilder("main")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(Void.TYPE)
            .addParameter(Array<String>::class.java, "args")
            .addStatement("\$T.out.println(\$S)", System::class.java, "Hello, JavaPoet!")
            .build()

        val helloWorld = TypeSpec.classBuilder("HelloWorld")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addMethod(main)
            .build()

        val javaFile = JavaFile.builder("com.example.helloworld", helloWorld)
            .build()

        try {
            javaFile.writeTo(filer)
        }catch (e: IOException){
            e.printStackTrace()
        }
    }
}