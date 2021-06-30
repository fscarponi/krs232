import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.log4j.BasicConfigurator
import java.lang.Thread.sleep
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SerialPortManagerTest {

    @Test
//    @RepeatedTest(8)
    fun `simpleSerialComTest`(): Unit = runBlocking {
        BasicConfigurator.configure()
        val port = SerialPortManager.portsMap[SerialPortManager.portsMap.keys.first()]!!
        val response = port.sendCommand("PROCESSO test messaggio base\n")
        assertEquals("PROCESSO test messaggio base\n", response)
    }

    @Test
    fun `concurrentSerialComTest`(): Unit = runBlocking<Unit> {
        BasicConfigurator.configure()
        val port = SerialPortManager.portsMap[SerialPortManager.portsMap.keys.first()]!!
        val job1 = launch {
            println("sono il processo 1")
            sleep(Random.nextLong(2000))
            repeat(10) {

                try {
                    val messaggio = "PROCESSO 1 messaggio fweawaefewafefwawefaaefwaefwaefwfaew $it\n"
                    val response = port.sendCommand(messaggio)
                    assertTrue {
                        println("test PROCESSO 1 $it->result: ${messaggio == response}")
                        messaggio == response
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        val job2 = launch {
            println("sono il processo 2")
            sleep(Random.nextLong(2000))
            repeat(10) {
                try {
                    val messaggio = "PROCESSO 2 messaggio fweawaefewafefwawefaaefwaefwaefwfaew $it\n"
                    val response = port.sendCommand(messaggio)
                    assertTrue {
                        println("test PROCESSO 2 $it->result: ${messaggio == response}")
                        messaggio == response
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

}
