# P2P Kotlin Multicast Chat

![Kotlin](https://img.shields.io/badge/Kotlin-000000?style=for-the-badge&logo=kotlin&logoColor=white)
![UDP Multicast](https://img.shields.io/badge/Multicast_P2P-FF0000?style=for-the-badge&logo=databricks&logoColor=white)
![CLI](https://img.shields.io/badge/CLI_Application-FFFFFF?style=for-the-badge&logo=gnometerminal&logoColor=black)

A decentralized (Peer-to-Peer) chat system designed for local area networks (LAN) using **UDP Multicast**. This project eliminates the need for a central server, connecting all nodes directly through the group IP `224.0.0.10`.

---

## Features

* **P2P Architecture:** Direct communication between machines on the local network via `MulticastSocket`. No intermediate servers, no single point of failure.
* **Custom Packet Protocol:** Manual serialization of messages into byte arrays, reducing network *overhead* and ensuring fast processing.
* **Automatic Emoji Mapping:** Real-time translation of ASCII shortcuts (e.g., `:)`, `>:(`, `<3`) into Unicode characters (🙂, 😠, ❤️).
* **ECHO System:** A dedicated thread sends an ECHO message across the network every 5 seconds to announce that the user remains online (laying the groundwork for future active user list implementations).
* **Asynchronous I/O:** Clear separation of concerns with independent threads for listening (`receiver`), sending (`sendMessages`), and heartbeats (`ECHO`).

---

## Network Packet Structure

To maximize efficiency, the chat does not send raw strings. It assembles a structured byte packet before dispatching it over the network:

```text
[ 1 Byte ] [ 1 Byte ] [ N Bytes ] [ 1 Byte ] [ M Bytes ]
   Type     NickSize    Nickname    MsgSize     Message
