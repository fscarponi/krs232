import jssc.SerialPort
import jssc.SerialPortList
import jssc.SerialPortTimeoutException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.apache.log4j.BasicConfigurator
import java.lang.Thread.sleep


object SerialPortManager {


    val protocolEndMessageSymbols = listOf('\n')
    val portsMap = mutableMapOf<String, SerialPort>().apply {
        SerialPortList.getPortNames().onEach {
            this[it] = SerialPort(it).apply {
                if (!this.isOpened) openPort()
                setParams(
                    SerialPortParams.BAUD_RATE,
                    SerialPortParams.DATA_BITS,
                    SerialPortParams.STOP_BITS,
                    SerialPortParams.PARITY
                )
                flowControlMode = SerialPortParams.FLOW_CONTROL
            }
        }
    }

    @Synchronized
    fun sendCommand(serialPort: SerialPort, text: String, responseTimeOut: Int = 3000): String = runBlocking {
        sleep(250)
        serialPort.writeString(text)
        serialPort.purgePort(SerialPort.PURGE_RXCLEAR)
        serialPort.purgePort(SerialPort.PURGE_TXCLEAR)
        val timeOutInstant = Clock.System.now().toEpochMilliseconds() + responseTimeOut
        val sb = StringBuilder("")
        while (protocolEndMessageSymbols.find {
                sb.endsWith(it)
            } == null || Clock.System.now().toEpochMilliseconds() >= timeOutInstant) {
            sleep(100)
            serialPort.readString()?.let { sb.append(it) }
            serialPort.purgePort(SerialPort.PURGE_RXCLEAR)
            serialPort.purgePort(SerialPort.PURGE_TXCLEAR)
        }
        if (Clock.System.now().toEpochMilliseconds() > timeOutInstant) throw SerialPortTimeoutException(
            serialPort.portName, "sendCommand", responseTimeOut
        )
        sb.toString()
    }

}

object SerialPortParams {
    const val BAUD_RATE: Int = SerialPort.BAUDRATE_9600
    const val DATA_BITS: Int = SerialPort.DATABITS_8
    const val STOP_BITS: Int = SerialPort.STOPBITS_1
    const val PARITY: Int = SerialPort.PARITY_NONE
    const val FLOW_CONTROL: Int = SerialPort.FLOWCONTROL_NONE
}


@OptIn(DelicateCoroutinesApi::class)
suspend fun main(): Unit = coroutineScope {
    BasicConfigurator.configure()
    val port = SerialPortManager.portsMap[SerialPortManager.portsMap.keys.first()]!!
    launch {
        println("sono il processo 1")
        repeat(10_000) {
            try {
                println(port.sendCommand("PROCESSO 1 messaggio fweawaefewafefwawefaaefwaefwaefwfaew $it\n"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    launch {
        println("sono il processo 2")
        repeat(10_000) {
            try {
                println(port.sendCommand("PROCESSO 2 messaggio fefew $it\n"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}

suspend fun SerialPort.sendCommand(text: String, responseTimeOut: Int = 3000): String {
    return SerialPortManager.sendCommand(SerialPortManager.portsMap[this.portName]!!, text, responseTimeOut)
}

