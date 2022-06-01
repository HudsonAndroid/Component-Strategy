package com.hudson.hrouter_annotation_processor.utils

import com.hudson.hrouter.annotation.enums.PageType
import javax.lang.model.element.Element
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 * 类型是否是合法的路由页面类型
 */
fun Element.isValidRoutePageType(typeUtils: Types?,
                                 elementsUtils: Elements?): PageType? {
    for(pageType in PageType.values()){
        // 判断是否命中可解析的路由类型（例如被注解的类是否是Activity类型）
        if(typeUtils?.isSubtype(this.asType(),
                elementsUtils?.getTypeElement(pageType.typePkg())?.asType()) == true){
            return pageType
        }
    }
    return null
}