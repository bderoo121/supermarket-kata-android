package supermarket.model

class ShoppingCart {

    private val items = mutableListOf<ProductQuantity>()
    private val productQuantities = mutableMapOf<Product, Double>()

    internal fun getItems(): List<ProductQuantity> = items.toList()

    fun addItemQuantity(product: Product, quantity: Double) {
        items.add(ProductQuantity(product, quantity))
        productQuantities[product] = (productQuantities[product] ?: 0.0) + quantity
    }

    internal fun handleOffers(receipt: Receipt, offers: List<Offer>, catalog: SupermarketCatalog) {
        offers.mapNotNull { it.getDiscount(this, catalog) }.forEach { receipt.addDiscount(it) }
    }

    fun quantityOf(product: Product): Double? {
        return productQuantities[product]
    }
}
