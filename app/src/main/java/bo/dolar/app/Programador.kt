package bo.dolar.app

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

/** Programa la consulta automática diaria a las 19:00 (hora del teléfono). */
object Programador {
    private const val NOMBRE = "consulta_diaria_19"
    private const val HORA = 19

    fun msHastaLas19(): Long {
        val ahora = Calendar.getInstance()
        val objetivo = (ahora.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, HORA)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!objetivo.after(ahora)) objetivo.add(Calendar.DAY_OF_YEAR, 1)
        return objetivo.timeInMillis - ahora.timeInMillis
    }

    private fun request(): OneTimeWorkRequest =
        OneTimeWorkRequestBuilder<ConsultaWorker>()
            .setInitialDelay(msHastaLas19(), TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)
            .build()

    /** Se llama al abrir la app: si ya hay una programada, la mantiene. */
    fun asegurar(ctx: Context) {
        WorkManager.getInstance(ctx)
            .enqueueUniqueWork(NOMBRE, ExistingWorkPolicy.KEEP, request())
    }

    /** Se llama desde el worker al terminar: deja programada la del día siguiente. */
    fun encadenarSiguiente(ctx: Context) {
        WorkManager.getInstance(ctx)
            .enqueueUniqueWork(NOMBRE, ExistingWorkPolicy.APPEND_OR_REPLACE, request())
    }
}

class ConsultaWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        return try {
            Repositorio.actualizar(applicationContext, Repositorio.ORIGEN_AUTO)
            Programador.encadenarSiguiente(applicationContext)
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry() // reintenta en ~10, 20, 40 min
            } else {
                Programador.encadenarSiguiente(applicationContext)
                Result.success()
            }
        }
    }
}
