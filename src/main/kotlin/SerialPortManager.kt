import jssc.*
import org.apache.log4j.BasicConfigurator
import java.lang.Thread.sleep

class SerialPortManager {
    init {
        val ports = SerialPortList.getPortNames().onEach {
            ports.add(SerialPort(it).apply {
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
        require(ports.isNotEmpty())
    }

    val ports = mutableListOf<SerialPort>()
}

object SerialPortParams {
    const val BAUD_RATE: Int = SerialPort.BAUDRATE_9600
    const val DATA_BITS: Int = SerialPort.DATABITS_8
    const val STOP_BITS: Int = SerialPort.STOPBITS_1
    const val PARITY: Int = SerialPort.PARITY_NONE
    const val FLOW_CONTROL: Int = SerialPort.FLOWCONTROL_NONE
}


fun main() {
    BasicConfigurator.configure();
    val portManager = SerialPortManager()
    val port = portManager.ports.first()
    port.writeString("ciao mario")
    println(port.readString())


}
