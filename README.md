# Component-Strategy
组件化方案优点：

- 充分体现高内聚，低耦合特性，益于后续维护升级
- 各个组件单独测试，编译速度提高
- 功能业务重用
- 团队并行开发，效率提升

![组件化示例图](组件化示例图.png)

## 1.组件化的目标
- 1.各个组件无耦合关系，相互独立，可拔插
- 2.组件可以单独测试验证或独立运行

组件构成上，由最上层的app壳 + 本身app主功能业务组件 + 常规业务组件（和基础业务组件） + 基础功能组件构成。

本身app主功能业务组件跟app本身功能关联度最大，与常规业务组件不同，能够被复用于其他应用的可能性更低。

[壳工程](app)作为“傀儡”，仅负责处理启动屏，和统筹依赖其他业务组件以及app主功能业务组件。

### 1.1 [各组件依赖版本管理问题](basic_gradle_config.gradle)
为了统一管理各个组件的依赖库版本，以及统一使用一个Gradle版本构建，因此新增一个统一的gradle配置文件（将该文件作为远程依赖，远程可视化视图配置参数将更便捷地控制版本）。 
### 1.2 业务组件的可测试性
为了确保业务组件本身可以单独进行除了基础的单元测试之外，还能进行GUI测试，因此应该确保业务组件本身的可应用化的特性。

因此需要手动控制部分代码文件，以确保在组件自我测试验证时保持应用的主体性，而在作为组件引入其他上传业务组件中时作为组件模块提供功能。

借助于Gradle的sourceSets和android的application和library特性来实现这一点。

#### 1）控制组件和应用的特性来回切换
继续在[公共Gradle配置文件]((basic_gradle_config.gradle))中新增是否集成的标记flag，通过该标记控制是组件还是应用。  

**注：为了考虑到组件自身的独立控制性，可以考虑将标记下沉给组件开发方控制，此处为了多个模块统一控制，使用公共Gradle配置文件。**

	flags = [
        // 控制业务组件是否集成，如果是false，业务组件将作为应用程序方式运行，即构建产物是apk
        isRelease: false
    ]

组件中的配置：
	// 1) 切换application和library
	plugins {
	    // 不能这样使用
	//    if(flags.isRelease){
	//        id 'com.android.application'
	//    }else{
	//        id 'com.android.library'
	//    }
	    id 'org.jetbrains.kotlin.android'
	}
	
	// 必须按照旧版Gradle的方式
	if(flags.isRelease){
	    apply plugin: 'com.android.application'
	}else{
	    apply plugin: 'com.android.library'
	}

	// 2)切换applicationId
	android {
	    defaultConfig {
	        if(!flags.isRelease){
	            applicationId "com.hudson.order"
	        }
	    }
	}

**注意：**

- 1.示例工程的Gradle版本是7.4.2，Gradle新增了plugins{}方式引入Gradle插件，但是内部不支持其他声明，包括If-else。 因此需要使用旧版的引入方式，即apply plugin
- 2.plugins的声明必须优先于其他声明。 例如把apply plugin段放到plugins前面将会报错

#### 2）控制Manifest中启动activity的状态
当业务组件被当成

## 参考文档
1. [工程-study_module](https://github.com/zouchanglin/study_module)
2. [视频-Android组件化实战](https://www.bilibili.com/video/BV1Ar4y1A7kh?spm_id_from=333.788.top_right_bar_window_custom_collection.content.click)