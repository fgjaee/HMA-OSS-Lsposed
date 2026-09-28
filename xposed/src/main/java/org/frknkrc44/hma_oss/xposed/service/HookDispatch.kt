package org.frknkrc44.hma_oss.xposed.service

import io.github.libxposed.api.XposedInterface.Chain

internal object HookDispatch {
    fun intercept(
        chain: Chain,
        after: Boolean,
        onError: (Throwable) -> Unit,
        callback: (HookFrame, ReturnValue) -> Unit,
    ): Any? {
        val frame = HookFrame(chain.executable, chain.thisObject, chain.args)
        var originalResult: Any? = null
        var originalError: Throwable? = null
        if (after) {
            try {
                originalResult = frame.proceed(chain)
            } catch (error: Throwable) {
                originalError = error
            }
        }
        val value = ReturnValue(originalResult, originalError)
        try {
            callback(frame, value)
        } catch (error: Throwable) {
            onError(error)
            // Discard partial callback changes; never execute the original twice.
            if (after) {
                originalError?.let { throw it }
                return originalResult
            }
            return chain.proceed()
        }
        value.throwable?.let { throw it }
        return if (after || value.replace) value.result else frame.proceed(chain)
    }
}
