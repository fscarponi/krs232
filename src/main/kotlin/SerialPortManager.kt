import jssc.SerialPort
import jssc.SerialPortList
import jssc.SerialPortTimeoutException
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.Clock
import org.apache.log4j.BasicConfigurator
import java.lang.Thread.sleep


object SerialPortManager {

    val protocolEndMessageSymbols = listOf('\n', '\r')
    val ports = mutableListOf<SerialPort>().apply {
        SerialPortList.getPortNames().onEach {
            this.add(SerialPort(it).apply {
                if (!this.isOpened) openPort()
                setParams(
                    SerialPortParams.BAUD_RATE,
                    SerialPortParams.DATA_BITS,
                    SerialPortParams.STOP_BITS,
                    SerialPortParams.PARITY
                )
                flowControlMode = SerialPortParams.FLOW_CONTROL
            })
        }
    }


}

object SerialPortParams {
    const val BAUD_RATE: Int = SerialPort.BAUDRATE_9600
    const val DATA_BITS: Int = SerialPort.DATABITS_8
    const val STOP_BITS: Int = SerialPort.STOPBITS_1
    const val PARITY: Int = SerialPort.PARITY_NONE
    const val FLOW_CONTROL: Int = SerialPort.FLOWCONTROL_NONE
}


suspend fun main(): Unit = coroutineScope {
    BasicConfigurator.configure()
    val port = SerialPortManager.ports.first()
    repeat(10_000) {
        println(port.sendCommand("messaggio $it\n"))
    }

}

suspend fun SerialPort.sendCommand(text: String, responseTimeOut: Int = 3000): String {
    this.writeString(text)
    val timeOutInstant = Clock.System.now().toEpochMilliseconds() + responseTimeOut
    val sb = StringBuilder("")
    while (SerialPortManager.protocolEndMessageSymbols.find {
            sb.endsWith(it)
        } == null || Clock.System.now().toEpochMilliseconds() > timeOutInstant) {
        sleep(50)
        sb.append(readString())
    }
    if (Clock.System.now().toEpochMilliseconds() > timeOutInstant) throw SerialPortTimeoutException(
        this.portName, "sendCommand", responseTimeOut
    )
    return sb.toString()
}


