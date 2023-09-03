package kr.hqservice.framework.bukkit.core.coroutine.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

val Job.coroutineContext: CoroutineContext
    get() {
        return (this as CoroutineScope).coroutineContext
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