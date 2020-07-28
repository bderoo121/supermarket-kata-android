package supermarket.model

import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

sealed class Offer {
    abstract fun getDiscount(
        shoppingCart: ShoppingCart,
        catalog: SupermarketCatalog
    ): Discount?

    protected fun multiplierStringOrEmpty(numDiscounts: Int) =
        if (numDiscounts > 1) {
            " x${numDiscounts}"
        } else {
            ""
        }
}

class BundleOffer(
    private val description: String,
    private val products: List<Product>,
    private val discountPrice: Double
) : Offer()
{
    init {
        if (products.isEmpty()) throw IllegalArgumentException("Must have some products in bundle")
    }

    override fun getDiscount(shoppingCart: ShoppingCart, catalog: SupermarketCatalog): Discount? {
        val quantityOfBundleProducts = products.mapNotNull { shoppingCart.quantityOf(it) }
        return if (quantityOfBundleProducts.size == products.size) {
            // The whole bundle was there, so return the discount
            val normalBundlePrice = products.sumByDouble { catalog.getUnitPrice(it) }
            val numberOfBundles = quantityOfBundleProducts.min()?.toInt() ?: throw IllegalStateException()
            Discount(
                "$description${multiplierStringOrEmpty(numberOfBundles)}",
                (normalBundlePrice - discountPrice) * numberOfBundles
            )
        } else {
            null
        }
    }
}

abstract class SingleProductOffer(val product: Product) : Offer()

class PercentageOffer(product: Product, private val percentOff: Double) : SingleProductOffer(product) {
    override fun getDiscount(
        shoppingCart: ShoppingCart,
        catalog: SupermarketCatalog
    ): Discount? {
        val quantityOf = shoppingCart.quantityOf(product) ?: return null

        val unitPrice = catalog.getUnitPrice(product)
        return Discount("$percentOff% off (${product.name})", quantityOf * unitPrice * percentOff / 100.0)
    }
}

class ThreeForTwoOffer(product: Product) : SingleProductOffer(product) {
    init {
        if (product.unit != ProductUnit.Each) {
            throw IllegalArgumentException("Can only use ThreeForTwo with discrete items")
        }
    }
    override fun getDiscount(
        shoppingCart: ShoppingCart,
        catalog: SupermarketCatalog
    ): Discount? {
        val numThrees = shoppingCart.quantityOf(product)?.div(3)?.toInt() ?: 0
        if (numThrees < 1) return null
        val normalPrice = catalog.getUnitPrice(product)
        val discountAmount = normalPrice * numThrees // One off per three
        val multiplierString = multiplierStringOrEmpty(numThrees)
        return Discount("3 for 2 (${product.name})$multiplierString", discountAmount)
    }
}

class QuantityForAmountOffer(product: Product, private val discountQuantity: Int, private val price: Double) :
    SingleProductOffer(product) {
    override fun getDiscount(
        shoppingCart: ShoppingCart,
        catalog: SupermarketCatalog
    ): Discount? {
        val productQuantity = shoppingCart.quantityOf(product)
        val quantityInt = productQuantity?.toInt()?.let {
            if (it < discountQuantity) null else it
        } ?: return null

        val unitPrice = catalog.getUnitPrice(product)
        val discounted = price * (quantityInt / discountQuantity) + (quantityInt % discountQuantity) * unitPrice
        val normal = quantityInt * unitPrice
        return Discount("$discountQuantity for \$$price (${product.name})", normal - discounted)
    }
}

fun main() {
    val apples = Product("apples", ProductUnit.Each)
    val offer = QuantityForAmountOffer(apples, 2, 1.0)
    val cart = ShoppingCart()
    cart.addItemQuantity(apples, 3.0)
    val catalog = object : SupermarketCatalog {
        override fun addProduct(product: Product, price: Double) {
            TODO("Not yet implemented")
        }

        override fun getUnitPrice(product: Product): Double = 2.0
    }

    val discount = offer.getDiscount(cart, catalog)
    discount?.let {
        println(discount.description)
        println(discount.discountAmount)
    }
}
