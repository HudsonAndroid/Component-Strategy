package com.hudson.hrouter_api.repository

import android.content.Context
import android.util.LruCache
import com.hudson.hrouter_api.BuildConfig
import com.hudson.hrouter_api.repository.entry.ComponentGroupRepository
import com.hudson.hrouter_api.repository.entry.GroupPathRepository
import com.hudson.hrouter_api.utils.ClassUtils

/**
 * 路由表管理仓库
 * Created by Hudson on 2022/6/1.
 */
class HRouterRepository {
    @Volatile
    private var hasInit = false
    private val appRouteRepo = mutableMapOf<String, Class<out GroupPathRepository>>()

    /**
     * [GroupPathRepository]实例缓存
     */
    private val groupPathRepoCache = LruCache<String, GroupPathRepository>(64)

    companion object{
        private const val ComponentGroupRepoFilePrefix = "HRouterComponent"
    }

    /**
     * 初始化所有生成的组件路由类
     */
    fun initComponentGroupRepos(context: Context){
        if(hasInit) return
        synchronized(HRouterRepository::class.java){
            if(!hasInit){
                val routeGenPkg = BuildConfig.routeGenPkg
                val clazzSet = ClassUtils.getFileNameByPackageName(
                    context,
                    routeGenPkg
                )
                for(clazz in clazzSet){
                    if(clazz.startsWith("$routeGenPkg.$ComponentGroupRepoFilePrefix")){
                        // 如果是组件Group路由表，加载该类，并合并到现有集合中
                        val instance = Class.forName(clazz)
                            .getConstructor()
                            .newInstance() as ComponentGroupRepository
                        appRouteRepo.putAll(instance.getGroupRepository())
                    }
                }
                hasInit = true
            }
        }
    }

    fun findDestination(group: String, path: String): Class<*>? {
        if(!hasInit) throw IllegalStateException("请先初始化HRouter!")
        val pathRepository = findGroupPathRepo(group).getPathRepository()
        return pathRepository[path]?.pageClazz
    }

    /**
     * 查找对应Group的route集合
     */
    private fun findGroupPathRepo(group: String): GroupPathRepository {
        var repo = groupPathRepoCache[group]
        if(repo == null){
            val repoClazz = appRouteRepo[group]
            if(repoClazz != null){
                repo = repoClazz.getConstructor().newInstance()
                groupPathRepoCache.put(group, repo)
                return repo
            }
            // 类不存在，即没有这个group对应的路由集合
            throw RuntimeException("没有找到对应的路由分组")
        }
        return repo
    }
}