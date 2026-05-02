package bonfim.jordan

import java.io.PrintStream
import java.lang.Thread.sleep
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.nio.charset.StandardCharsets
import java.util.Scanner
import kotlin.concurrent.thread

fun main() {

    val CHECK_UNIQUE_USERNAME = true
    System.setOut(PrintStream(System.out, true, StandardCharsets.UTF_8.name()))

    val asciiToEmojiMap = hashMapOf(
        ":)" to "🙂",    ":-)" to "🙂",
        ":(" to "🙁",    ":-(" to "🙁",
        ";)" to "😉",    ";-)" to "😉",
        ":D" to "😃",    ":-D" to "😃",
        ":P" to "😛",    ":-P" to "😛",
        ":O" to "😮",    ":-O" to "😮",
        ":/" to "😕",    ":-/" to "😕",
        ":'(" to "😢",   ":-|" to "😐",
        ":|" to "😐",    ">:" to "😠",
        ">:( " to "😠",  ">:)" to "😈",
        ">:( " to "😠",  ">:( " to "😠",
        ">:( " to "😠",  ">:(" to "😠",
        ":-X" to "🤐",   ":X" to "🤐",
        "B)" to "😎",    "B-)" to "😎",
        "8)" to "😎",    "8-)" to "😎",
        "^_^" to "😊",   "-_-" to "😑",
        "o_O" to "🤨",   "O_o" to "🤨",
        "<3" to "❤️",    "</3" to "💔",
        ":3" to "😺",    ":-3" to "😺",
        ":>" to "😏",    ":<" to "😣",
        "._." to "😶",   "T_T" to "😭",
    )

    val scanner = Scanner(System.`in`)

    println("CHAT P2P MULTICAST")

    val groupIp = "224.0.0.10"
    val port = 5000

    val group = InetAddress.getByName(groupIp)
    val socket = MulticastSocket(port)

    socket.joinGroup(group,)

    println("Seu usuário:")
    val myNickname = scanner.nextLine()
    val nicknameBytes = myNickname.toByteArray()
    val nickSize = nicknameBytes.size.toByte()

    println("[Conectado ao grupo $groupIp na porta $port]")
    println("Tudo que você digitar será enviado para todos na rede. Digite '/sair' para encerrar.\n")

    // Thread para receber mensagens
    thread(start = true, name = "receiver") {
        val buffer = ByteArray(1024)
        while (!socket.isClosed) {
            try {
                val packet = DatagramPacket(buffer, buffer.size)
                socket.receive(packet)

                val senderIp = packet.address.hostAddress
                val myIp = InetAddress.getLocalHost().hostAddress
                val message = String(packet.data, 0, packet.length)

                // Evita imprimir a própria mensagem
                if (senderIp != myIp ) {

                    val data = packet.data

                    var index = 0

                    val msgType = data[index++]

                    //println(msgType)
                    if(msgType.toInt() == 4){
                        //println("ECHO RECEIVED")
                        //println(">> ")
                    }

                    // we need to use "and 0XFF" because Kotlin byte is signed by default
                    val nickSize = data[index++].toInt() and 0xFF

                    val nick = String(data, index, nickSize)
                    index += nickSize

                    val msgSize = data[index++].toInt() and 0xFF

                    val msg = String(data, index, msgSize)


                    // We may need this validation if we are executing on the same machine...
                    // Senderip e myIP might be the same
                    // We use nickname condition in this situation.
                    // But unique nickname feature has not been implemented.
                    if( !myNickname.equals(nick, ignoreCase = true) && CHECK_UNIQUE_USERNAME) {

                        if(msgType.toInt() == 1 || msgType.toInt() == 3) {
                            println("\n[${senderIp}]: $msg")
                            print(">> ")
                        }

                        if(msgType.toInt() == 2){
                            val emoji = asciiToEmojiMap.getOrDefault(msg, "(Emoji não encontrado)")
                            println("\n[${senderIp}]: $emoji")
                            print(">> ")
                        }

                    }
                }
            } catch (e: Exception) {
                if (!socket.isClosed) println("Erro: ${e.message}")
            }
        }
    }

    // Loop  para ENVIAR mensagens
    fun sendMessages(){

        val asciiEmojis = arrayOf(
            ":)", ":-)", ":(", ":-(", ";)", ";-)",
            ":D", ":-D", ":P", ":-P", ":O", ":-O",
            ":/", ":-/", ":'(", ":-|", ":|",
            ">:(", ">:)", ":-X", ":X",
            "B)", "B-)", "8)", "8-)",
            "^_^", "-_-", "o_O", "O_o",
            "<3", "</3",
            ":3", ":-3",
            ":>", ":<",
            "._.", "T_T"
        )

        while (true) {
            print(">> ")
            val msg = scanner.nextLine()

            if (msg.equals("/sair", ignoreCase = true)) {
                break
            }

            var msgType = 1 // MENSAGEM NORMAL

            if(asciiEmojis.contains(msg)){
                msgType = 2 // EMOJI
            }

            if(msg.startsWith("http://") || msg.startsWith("https://")){
                msgType = 3 // URL
            }


            val type: Byte = msgType.toByte()  // tipo da mensagem (ex: chat)

            val messageBytes = msg.toByteArray()

            val msgSize = messageBytes.size.toByte()

            // monta o buffer final
            val bytes = ByteArray(1 + 1 + nicknameBytes.size + 1 + messageBytes.size)

            var index = 0

            bytes[index++] = type
            bytes[index++] = nickSize

            for (b in nicknameBytes) {
                bytes[index++] = b
            }

            bytes[index++] = msgSize

            for (b in messageBytes) {
                bytes[index++] = b
            }

            if (bytes.isNotEmpty()) {
                // Envia o pacote para o IP do Grupo
                val packet = DatagramPacket(bytes, bytes.size, group, port)
                socket.send(packet)
            }

        }
    }

    thread(start = true, name = "ECHO") {

        while (true) {

            val type: Byte = 4  // tipo da mensagem (ECHO)

            val messageBytes = "".toByteArray()

            val msgSize = messageBytes.size.toByte()

            // monta o buffer final
            val bytes = ByteArray(1 + 1 + nicknameBytes.size + 1 + messageBytes.size)

            var index = 0

            bytes[index++] = type
            bytes[index++] = nickSize

            for (b in nicknameBytes) {
                bytes[index++] = b
            }

            bytes[index++] = msgSize

            for (b in messageBytes) {
                bytes[index++] = b
            }

            if (bytes.isNotEmpty()) {
                // Envia o pacote para o IP do Grupo
                val packet = DatagramPacket(bytes, bytes.size, group, port)
                socket.send(packet)
            }
            sleep(5000)
        }
    }

    sendMessages()
    // Encerrar
    socket.leaveGroup(group) // Sai do grupo
    socket.close()
    println("Chat encerrado.")
}