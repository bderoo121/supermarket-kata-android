package supermarket

import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.fail
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
import supermarket.model.Receipt
import supermarket.model.ShoppingCart
import supermarket.model.Teller
import supermarket.model.ThreeForTwoOffer
import org.hamcrest.CoreMatchers.`is` as Is

class ProductTest {
    private val catalog = FakeCatalog()
    private val cart = ShoppingCart()
    private val teller = Teller(catalog)
    private var receipt: Receipt? = null

    @Before
    fun setup() {
        TestUtils.setupCatalog(catalog)
    }

    @After
    fun tearDown() {
        // No teardown necessary, but print receipt while we're at it.
        receipt?.let {
            println(ReceiptPrinter().printReceipt(receipt!!))
        }
    }

    @Test
    fun `Require items in cart to apply discounts`() {
        teller.addSpecialOffer(PercentageOffer(toothbrush, 10.0))

        receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt!!.getDiscounts()

        assertThat(discounts.size, Is(0))
    }

    @Test
    fun `Require special offers to apply discounts`() {
        cart.addItemQuantity(toothbrush, 25.0)

        receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt!!.getDiscounts()

        assertThat(discounts.size, Is(0))
    }

    @Test
    fun `Discounts are only applied to relevant products`() {
        cart.addItemQuantity(oranges, 2.0)
        cart.addItemQuantity(apples, 1.0)
        teller.addSpecialOffer(PercentageOffer(oranges, 25.0))
        teller.addSpecialOffer(ThreeForTwoOffer(toothbrush))

        receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt!!.getDiscounts()

        assertThat(discounts.size, Is(1))
        assertThat(discounts[0].description, containsString("oranges"))
    }

    @Test
    fun `Apply multiple discounts for different products`() {
        cart.addItemQuantity(oranges, 5.0)
        cart.addItemQuantity(apples, 3.0)
        teller.addSpecialOffer(PercentageOffer(apples, 10.0))
        teller.addSpecialOffer(QuantityForAmountOffer(oranges, 4, 5.0))

        receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt!!.getDiscounts()

        assertThat(discounts.size, Is(2))
        assertThat(discounts[0].description, containsString("apples"))
        assertThat(discounts[0].discountAmount, Is(0.60))
        assertThat(discounts[1].description, containsString("oranges"))
        assertThat(discounts[1].discountAmount, Is(3.00))
    }

    @Test
    fun `Apply multiple discounts for same product`() {
        cart.addItemQuantity(oranges, 5.0)
        teller.addSpecialOffer(PercentageOffer(oranges, 10.0))
        teller.addSpecialOffer(QuantityForAmountOffer(oranges, 4, 5.0))

        receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt!!.getDiscounts()

        assertThat(discounts.size, Is(2))
        assertThat(discounts[0].description, containsString("oranges"))
        assertThat(discounts[0].discountAmount, Is(1.00))
        assertThat(discounts[1].description, containsString("oranges"))
        assertThat(discounts[1].discountAmount, Is(3.00))
    }

    @Test
    fun `Bundle offer`() {
        cart.addItemQuantity(toothbrush, 1.0)
        cart.addItemQuantity(toothpaste, 1.0)
        teller.addSpecialOffer(BundleOffer("Dental bundle", listOf(toothbrush, toothpaste), 3.00))

        receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt!!.getDiscounts()

        assertThat(discounts.size, Is(1))
        assertThat(discounts[0].discountAmount, Is(1.41))
    }

    @Test
    fun `Bundle offer without enough items`() {
        cart.addItemQuantity(toothbrush, 1.0)
        cart.addItemQuantity(toothpaste, 1.0)

        teller.addSpecialOffer(BundleOffer("Dental bundle", listOf(toothbrush, toothpaste, oranges), 3.00))
        receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt!!.getDiscounts()

        assertThat(discounts.size, Is(0))
    }

    @Test
    fun `Bundle offer applied multiple times`() {
        cart.addItemQuantity(toothbrush, 3.0)
        cart.addItemQuantity(toothpaste, 2.0)

        teller.addSpecialOffer(BundleOffer("Dental bundle", listOf(toothbrush, toothpaste), 3.00))
        receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt!!.getDiscounts()

        assertThat(discounts.size, Is(1))
        assertThat(discounts[0].description, containsString("x2"))
        assertThat(discounts[0].discountAmount, Is(2.82))
    }

    @Test
    fun `Half off discount`() {
        cart.addItemQuantity(apples, 2.0)
        teller.addSpecialOffer(PercentageOffer(apples, 50.0))
        receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt!!.getDiscounts()

        assertThat(discounts[0].discountAmount, Is(1.99))
    }

    @Test
    fun `Percent off discount without rounding`() {
        cart.addItemQuantity(oranges, 2.0)
        teller.addSpecialOffer(PercentageOffer(oranges, 25.0))

        receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt!!.getDiscounts()

        assertThat(discounts[0].discountAmount, Is(1.00))
    }

    @Test
    fun `Percent off discount with rounding`() {
        cart.addItemQuantity(toothbrush, 1.0)
        teller.addSpecialOffer(PercentageOffer(toothbrush, 10.0))

        receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt!!.getDiscounts()

        assertThat(discounts[0].discountAmount, Is(0.10))
    }

    @Test
    fun `Percent off non-int quantity`() {
        cart.addItemQuantity(flour, 2.5)
        teller.addSpecialOffer(PercentageOffer(flour, 50.0))

        receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt!!.getDiscounts()

        assertThat(discounts[0].discountAmount, Is(3.13))
    }

    @Test
    fun `Three for two offer`() {
        cart.addItemQuantity(apples, 3.0)
        teller.addSpecialOffer(ThreeForTwoOffer(apples))

        receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt!!.getDiscounts()

        assertThat(discounts[0].discountAmount, Is(1.99))
    }

    @Test
    fun `Three for two with extra product`() {
        cart.addItemQuantity(toothbrush, 4.0)
        teller.addSpecialOffer(ThreeForTwoOffer(toothbrush))
        receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt!!.getDiscounts()
        assertThat(discounts.size, Is(1))
        assertThat(discounts[0].discountAmount, Is(0.99))
    }

    @Test
    fun `Three for two offer without enough product`() {
        cart.addItemQuantity(apples, 2.0)
        teller.addSpecialOffer(ThreeForTwoOffer(apples))

        receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt!!.getDiscounts()

        assertThat(discounts.size, Is(0))
    }

    @Test
    fun `Three for two offer applied multiple times`() {
        cart.addItemQuantity(apples, 8.0)
        teller.addSpecialOffer(ThreeForTwoOffer(apples))

        receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt!!.getDiscounts()

        assertThat(discounts.size, Is(1))
        assertThat(discounts[0].description, containsString("x2"))
        assertThat(discounts[0].discountAmount, Is(3.98))
    }

    @Test
    fun `Three for two offer split over multiple additions`() {
        cart.addItemQuantity(oranges, 1.0)
        cart.addItemQuantity(oranges, 2.0)
        teller.addSpecialOffer(ThreeForTwoOffer(oranges))

        receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt!!.getDiscounts()

        assertThat(discounts[0].discountAmount, Is(2.00))
    }

    @Test
    fun `Three for two offer invalid for products using kilos`() {
        try {
            teller.addSpecialOffer(ThreeForTwoOffer(flour))
            fail()
        } catch (e: IllegalArgumentException) {
            // Hooray for exceptions!
        }
    }

    @Test
    fun `Quantity for amount offer`() {
        cart.addItemQuantity(toothbrush, 5.0)
        teller.addSpecialOffer(QuantityForAmountOffer(toothbrush, 5, 3.00))

        receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt!!.getDiscounts()

        assertThat(discounts[0].discountAmount, Is(1.95))
    }

    @Test
    fun `Quantity for amount offer without enough product`() {
        cart.addItemQuantity(toothbrush, 5.0)
        teller.addSpecialOffer(QuantityForAmountOffer(toothbrush, 6, 3.00))

        receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt!!.getDiscounts()

        assertThat(discounts.size, Is(0))
    }

    @Test
    fun `Quantity for amount offer applied multiple times`() {
        cart.addItemQuantity(toothbrush, 5.0)
        teller.addSpecialOffer(QuantityForAmountOffer(toothbrush, 2, 1.62))

        receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt!!.getDiscounts()

        assertThat(discounts.size, Is(1))
        assertThat(discounts[0].discountAmount, Is(0.72))
    }

    @Test
    fun `Quantity for amount offer split over multiple additions`() {
        cart.addItemQuantity(oranges, 2.0)
        cart.addItemQuantity(oranges, 3.0)
        teller.addSpecialOffer(QuantityForAmountOffer(oranges, 4, 6.00))

        receipt = teller.checksOutArticlesFrom(cart)
        val discounts = receipt!!.getDiscounts()

        assertThat(discounts[0].discountAmount, Is(2.00))
    }
}
