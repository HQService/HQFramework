package kr.hqservice.framework.bukkit.core.coroutine.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

val Job.coroutineContext: CoroutineContext
    get() {
        return try {
            (this as CoroutineScope).coroutineContext
        } catch (_: Exception) {
            EmptyCoroutineContext
        }
    }

val Job.childrenAll: List<Job>
    get() {
        val resultJobs: MutableList<Job> = mutableListOf()
        fun findChildrenJobsAsFlatAndStoreTo(job: Job, result: MutableList<Job>) {
            for (childJob in job.children) {
                result.add(childJob)
                findChildrenJobsAsFlatAndStoreTo(childJob, result)
            }
        }

        findChildrenJobsAsFlatAndStoreTo(this, resultJobs)
        return resultJobs
    }