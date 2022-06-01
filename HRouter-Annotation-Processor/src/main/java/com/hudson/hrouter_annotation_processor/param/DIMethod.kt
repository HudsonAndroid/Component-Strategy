package com.hudson.hrouter_annotation_processor.param

import com.hudson.hrouter.annotation.Parameter
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import java.lang.IllegalArgumentException
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic

/**
 * 构建注入的方法
 *
    @Override
    public void inject(Object targetObject) {
        OrderMainActivity t = (OrderMainActivity)targetObject;

        t.name = t.getIntent().getStringExtra("name");
        t.age = t.getIntent().getIntExtra("age");
        ....
    }
 * Created by Hudson on 2022/5/31.
 */
class DIMethod private constructor(builder: DIMethod.Builder){
    private val messager = builder.messager
    private val diTypeElement = builder.diTypeElement
    private val fieldElements = builder.fieldElements
    private val methodSpec: MethodSpec.Builder

    companion object{
        private const val parameterName = "targetObject"
    }

    init {
        val parameter = ParameterSpec.builder(TypeName.OBJECT, parameterName).build()

        methodSpec = MethodSpec.methodBuilder("inject")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(parameter)

        addFirstLineCode()

        fieldElements?.forEach{
            addInjectCode(it)
        }
    }

    /**
     * OrderMainActivity t = (OrderMainActivity)targetObject;
     */
    private fun addFirstLineCode(){
        val className = ClassName.get(diTypeElement)
        methodSpec.addStatement("\$T t = (\$T)$parameterName", className, className)
    }

    /**
     * 逐个属性注入的代码逻辑
     *
     *  t.name = t.getIntent().getStringExtra("name");
     */
    private fun addInjectCode(element: Element){
        // 获取注解的值，也就是route参数的key值 （即字段名可能与route参数名不同）
        var paramName = element.getAnnotation(Parameter::class.java).name
        // 如果使用者没有设置route参数名，则默认使用字段名
        val fieldName = element.simpleName.toString()
        paramName = paramName.ifEmpty {
            fieldName
        }

        val statement = StringBuilder("t.$fieldName = t.getIntent().")

        val mirrorType = element.asType()

        val defaultValue = "t.$fieldName"

        val intentTypeGetter = getIntentTypeGetter(mirrorType, paramName, defaultValue)
        if(intentTypeGetter == null){
            // 最后判断是否是String类型
            if(mirrorType.toString().equals("java.lang.String", ignoreCase = true)){
                statement.append("getStringExtra(\"$paramName\")")
            }else{
                messager?.printMessage(Diagnostic.Kind.ERROR, "不支持的路由参数类型: $mirrorType")
            }
        }else{
            statement.append(intentTypeGetter)
        }

        methodSpec.addStatement(statement.toString())
    }

    private fun getIntentTypeGetter(fieldType: TypeMirror, paramName: String, defaultValue: String): String? {
        val kind = fieldType.kind
        val mirrorTypeClazz = fieldType.toString()
        return when{
            (kind == TypeKind.INT ||
                    mirrorTypeClazz.equals("java.lang.Integer", ignoreCase = true)) ->
                "getIntExtra(\"$paramName\", ($defaultValue == null ? -1 : $defaultValue))"

            (kind == TypeKind.BOOLEAN ||
                    mirrorTypeClazz.equals("java.lang.Boolean", ignoreCase = true)) ->
                "getBooleanExtra(\"$paramName\", ($defaultValue == null ? false : $defaultValue))"

            (kind == TypeKind.FLOAT ||
                    mirrorTypeClazz.equals("java.lang.Float", ignoreCase = true)) ->
                "getFloatExtra(\"$paramName\", ($defaultValue == null ? -1f : $defaultValue))"

            (kind == TypeKind.DOUBLE ||
                    mirrorTypeClazz.equals("java.lang.Double", ignoreCase = true)) ->
                "getDoubleExtra(\"$paramName\", ($defaultValue == null ? -1.0 : $defaultValue)"
            else -> null
        }
    }

    class Builder{
        internal var messager: Messager? = null
            private set

        internal var diTypeElement: TypeElement? = null
            private set

        internal var fieldElements: List<Element>? = null
            private set

        fun messager(messager: Messager?) = apply {
            this.messager = messager
        }

        fun diTypeElement(diTypeElement: TypeElement) = apply {
            this.diTypeElement = diTypeElement
        }

        fun fieldElements(fieldElements: List<Element>) = apply {
            this.fieldElements = fieldElements
        }

        fun build(): MethodSpec {
            if(diTypeElement == null){
                throw IllegalArgumentException("被注入的类不能为空")
            }
            if(fieldElements.isNullOrEmpty()){
                throw  IllegalArgumentException("路由参数不能为空")
            }
            return DIMethod(this).methodSpec.build()
        }
    }
}