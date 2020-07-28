package supermarket.model

class Teller(private val catalog: SupermarketCatalog) {
    private val offers = mutableListOf<Offer>()

    fun addSpecialOffer(offer: Offer) {
        offers.add(offer)
    }

    fun checksOutArticlesFrom(theCart: ShoppingCart): Receipt {
        val receipt = Receipt()
        val productQuantities = theCart.getItems()
        for (pq in productQuantities) {
            val p = pq.product
            val quantity = pq.quantity
            val unitPrice = catalog.getUnitPrice(p)
            val price = quantity * unitPrice
            receipt.addProduct(p, quantity, unitPrice, price)
        }
        theCart.handleOffers(receipt, offers, catalog)

        return receipt
    }

}
