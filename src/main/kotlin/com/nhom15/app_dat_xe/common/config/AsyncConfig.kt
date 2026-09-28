package  com.nhom15.app_dat_xe.common.config

import org.slf4j.LoggerFactory
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor
import java.util.concurrent.ThreadPoolExecutor

/** Thread pool cho các @Async listener (matching, gửi thông báo...) để không chặn request thread. */
@Configuration
@EnableAsync
class AsyncConfig : AsyncConfigurer {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean(name = ["eventExecutor"])
    fun eventExecutor(): ThreadPoolTaskExecutor = ThreadPoolTaskExecutor().apply {
        corePoolSize = 4
        maxPoolSize = 16
        queueCapacity = 500
        setThreadNamePrefix("event-")
        // Hàng đợi đầy thì chạy ngay trên thread gọi, không làm mất event
        setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
        setWaitForTasksToCompleteOnShutdown(true)
        setAwaitTerminationSeconds(20)
        initialize()
    }

    /** @Async không chỉ tên executor sẽ dùng pool này. */
    override fun getAsyncExecutor(): Executor = eventExecutor()

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler =
        AsyncUncaughtExceptionHandler { ex, method, _ ->
            log.error("Lỗi trong tác vụ @Async {}: {}", method.name, ex.message, ex)
        }
}
