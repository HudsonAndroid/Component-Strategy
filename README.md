# Component-Strategy
组件化方案优点：

- 充分体现高内聚，低耦合特性，益于后续维护升级
- 各个组件单独测试，编译速度提高
- 功能业务重用
- 团队并行开发，效率提升

![组件化示例图](组件化示例图.png)

## 组件化的目标
- 1.各个组件无耦合关系，相互独立，可拔插
- 2.组件可以单独测试验证或独立运行

## [各组件依赖版本管理问题](basic_gradle_config.gradle)
为了统一管理各个组件的依赖库版本，以及统一使用一个Gradle版本构建，因此新增一个统一的gradle配置文件（将该文件作为远程依赖，远程可视化视图配置参数将更便捷地控制版本）。 


## 参考文档
1. [工程-study_module](https://github.com/zouchanglin/study_module)
2. [视频-Android组件化实战](https://www.bilibili.com/video/BV1Ar4y1A7kh?spm_id_from=333.788.top_right_bar_window_custom_collection.content.click)