package supermarket

import junit.framework.Assert.fail
import org.hamcrest.CoreMatchers.`is` as Is
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.closeTo
import org.junit.Before
import org.junit.Test
import supermarket.TestUtils.apples
import supermarket.TestUtils.flour
import supermarket.TestUtils.oranges
import supermarket.TestUtils.toothbrush
import supermarket.TestUtils.toothpaste
import supermarket.model.BundleOffer
import supermarket.model.PercentageOffer
import supermarket.model.QuantityForAmountOffer
import supermarket.model.ShoppingCart
import supermarket.model.Teller
import supermarket.model.ThreeForTwoOffer

class ProductTest {
    private val catalog = FakeCatalog()
    private val cart = ShoppingCart()
    private val teller = Teller(catalog)

    @Before
    fun setup() {
        TestUtils.setupCatalog(catalog)
    }

    @Test
    fun `Require items in cart to apply discounts`() {
        teller.addSpecialOffer(PercentageOffer(toothbrush, 10.0))

        val receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt.getDiscounts()

        assertThat(discounts.size, `is`(0))
    }

    @Test
    fun `Require special offers to apply discounts`() {
        cart.addItemQuantity(toothbrush, 25.0)

        val receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt.getDiscounts()

        assertThat(discounts.size, `is`(0))
    }

    @Test
    fun `Discounts are only applied to relevant products`() {
        cart.addItemQuantity(oranges, 2.0)
        cart.addItemQuantity(apples, 1.0)
        teller.addSpecialOffer(PercentageOffer(oranges, 25.0))
        teller.addSpecialOffer(ThreeForTwoOffer(toothbrush))

        val receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt.getDiscounts()

        assertThat(discounts.size, `is`(1))
        assertThat(discounts[0].product, `is`(oranges))
    }

    @Test
    fun `Apply multiple discounts for different products`() {
        cart.addItemQuantity(oranges, 5.0)
        cart.addItemQuantity(apples, 3.0)
        teller.addSpecialOffer(PercentageOffer(apples, 10.0))
        teller.addSpecialOffer(QuantityForAmountOffer(oranges, 4, 5.0))

        val receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt.getDiscounts()

        assertThat(discounts.size, `is`(2))
        assertThat(discounts[0].product, `is`(apples))
        assertThat(discounts[0].discountAmount, `is`(0.60))
        assertThat(discounts[1].product, `is`(oranges))
        assertThat(discounts[1].discountAmount, `is`(3.00))
    }

    @Test
    fun `Apply multiple discounts for same product`() {
        cart.addItemQuantity(oranges, 5.0)
        teller.addSpecialOffer(PercentageOffer(oranges, 10.0))
        teller.addSpecialOffer(QuantityForAmountOffer(oranges, 4, 5.0))

        val receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt.getDiscounts()

        assertThat(discounts.size, `is`(2))
        assertThat(discounts[0].product, `is`(oranges))
        assertThat(discounts[0].discountAmount, `is`(1.00))
        assertThat(discounts[1].product, `is`(oranges))
        assertThat(discounts[1].discountAmount, `is`(3.00))
    }

    @Test
    fun `single bundle`() {
        cart.addItemQuantity(toothbrush, 1.0)
        cart.addItemQuantity(toothpaste, 1.0)

        teller.addSpecialOffer(BundleOffer("Dental bundle", listOf(toothbrush, toothpaste), 3.00))
        val receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt.getDiscounts()
        assertThat(discounts.size, Is(1))
        assertThat(discounts[0].discountAmount, Is(closeTo(1.41, 0.00001)))
        println(ReceiptPrinter().printReceipt(receipt))
    }

    @Test
    fun `double bundle`() {
        cart.addItemQuantity(toothbrush, 3.0)
        cart.addItemQuantity(toothpaste, 2.0)

        teller.addSpecialOffer(BundleOffer("Dental bundle", listOf(toothbrush, toothpaste), 3.00))
        val receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt.getDiscounts()
        assertThat(discounts.size, Is(1))
        assertThat(discounts[0].discountAmount, Is(closeTo(2.82, 0.00001)))
        println(ReceiptPrinter().printReceipt(receipt))
    }

    @Test
    fun threeForTwo() {
        cart.addItemQuantity(toothbrush, 4.0)
        teller.addSpecialOffer(ThreeForTwoOffer(toothbrush))
        val receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt.getDiscounts()
        assertThat(discounts.size, Is(1))
        assertThat(discounts[0].discountAmount, Is(closeTo(0.99, 0.00001)))
        println(ReceiptPrinter().printReceipt(receipt))
    }


    @Test
    fun `Half off discount`() {
        cart.addItemQuantity(apples, 2.0)
        teller.addSpecialOffer(PercentageOffer(apples, 50.0))
        val receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt.getDiscounts()

        assertThat(discounts[0].discountAmount, `is`(1.99))
    }

    @Test
    fun `Percent off discount without rounding`() {
        cart.addItemQuantity(oranges, 2.0)
        teller.addSpecialOffer(PercentageOffer(oranges, 25.0))

        val receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt.getDiscounts()

        assertThat(discounts[0].discountAmount, `is`(1.00))
    }

    @Test
    fun `Percent off discount with rounding`() {
        cart.addItemQuantity(toothbrush, 1.0)
        teller.addSpecialOffer(PercentageOffer(toothbrush, 10.0))

        val receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt.getDiscounts()

        assertThat(discounts[0].discountAmount, `is`(0.10))
    }

    @Test
    fun `Percent off non-int quantity`() {
        cart.addItemQuantity(flour, 2.5)
        teller.addSpecialOffer(PercentageOffer(flour, 50.0))

        val receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt.getDiscounts()

        assertThat(discounts[0].discountAmount, `is`(3.13))
    }

    @Test
    fun `Three for two offer`() {
        cart.addItemQuantity(apples, 3.0)
        teller.addSpecialOffer(ThreeForTwoOffer(apples))

        val receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt.getDiscounts()

        assertThat(discounts[0].discountAmount, `is`(3.13))
    }

    @Test
    fun `Three for two offer without enough product`() {
        cart.addItemQuantity(apples, 2.0)
        teller.addSpecialOffer(ThreeForTwoOffer(apples))

        val receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt.getDiscounts()

        assertThat(discounts.size, `is`(0))
    }

    @Test
    fun `Three for two offer applied multiple times`() {
        cart.addItemQuantity(apples, 8.0)
        teller.addSpecialOffer(ThreeForTwoOffer(apples))

        val receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt.getDiscounts()

        assertThat(discounts.size, `is`(1))
        assertThat(discounts[0].discountAmount, `is`(3.98))
    }

    @Test
    fun `Three for two offer split over multiple additions`() {
        cart.addItemQuantity(oranges, 1.0)
        cart.addItemQuantity(oranges, 2.0)
        teller.addSpecialOffer(ThreeForTwoOffer(oranges))

        val receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt.getDiscounts()

        assertThat(discounts[0].discountAmount, `is`(2.00))
    }

    @Test
    fun `Three for two invalid for products using kilos`() {
        try {
            teller.addSpecialOffer(ThreeForTwoOffer(flour))
            fail()
        } catch (e: IllegalArgumentException) {
        }
    }

    @Test
    fun `Quantity for amount offer`(){
        cart.addItemQuantity(toothbrush, 5.0)
        teller.addSpecialOffer(QuantityForAmountOffer(toothbrush, 5, 3.00))

        val receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt.getDiscounts()

        assertThat(discounts[0].discountAmount, `is`(1.95))
    }

    @Test
    fun `Quantity for amount offer without enough product`(){
        cart.addItemQuantity(toothbrush, 5.0)
        teller.addSpecialOffer(QuantityForAmountOffer(toothbrush, 6, 3.00))

        val receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt.getDiscounts()

        assertThat(discounts.size, `is`(0))
    }

    @Test
    fun `Quantity for amount offer applied multiple times`(){
        cart.addItemQuantity(toothbrush, 5.0)
        teller.addSpecialOffer(QuantityForAmountOffer(toothbrush, 2, 1.62))

        val receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt.getDiscounts()

        assertThat(discounts.size, `is`(1))
        assertThat(discounts[0].discountAmount, `is`(0.72))
    }

    @Test
    fun `Quantity for amount offer split over multiple additions`(){
        cart.addItemQuantity(oranges, 2.0)
        cart.addItemQuantity(oranges, 3.0)
        teller.addSpecialOffer(QuantityForAmountOffer(oranges, 4, 6.00))

        val receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt.getDiscounts()

        assertThat(discounts[0].discountAmount, `is`(2.00))
    }
}
