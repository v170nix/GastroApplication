package net.arwix.gastro.library.data

data class OrderItem constructor(
    val name: String = "",
    val price: Long = 0L,
    val count: Int = 0
) {
    constructor() : this("", 0L, 0)
}