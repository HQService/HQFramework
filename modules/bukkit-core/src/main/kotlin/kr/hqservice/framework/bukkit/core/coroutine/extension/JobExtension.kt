package kr.hqservice.framework.bukkit.core.coroutine.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

val Job.coroutineContext: CoroutineContext
    get() {
        return if (this is CoroutineScope) this.coroutineContext
        else this
    }

val Job.childrenAll: List<Job>
    get() {
        val resultJobs: MutableList<Job> = mutableListOf()
        fun findChildrenJobsAsFlatAndStoreTo(job: Job, result: MutableList<Job>) {
            for (childJob in job.children) {
                findChildrenJobsAsFlatAndStoreTo(childJob, result)
            }
        }

        findChildrenJobsAsFlatAndStoreTo(this, resultJobs)
        return resultJobs
    }