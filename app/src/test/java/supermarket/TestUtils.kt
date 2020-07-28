package supermarket

import supermarket.model.Product
import supermarket.model.ProductUnit
import supermarket.model.SupermarketCatalog

object TestUtils {
    val toothbrush = Product("toothbrush", ProductUnit.Each)
    val toothpaste = Product("toothpaste", ProductUnit.Each)
    val oranges = Product("oranges", ProductUnit.Each)
    val apples = Product("apples", ProductUnit.Each)
    val flour = Product("flour", ProductUnit.Kilo)

    fun setupCatalog(catalog: SupermarketCatalog) {
        catalog.addProduct(toothbrush, 0.99)
        catalog.addProduct(toothpaste, 3.42)
        catalog.addProduct(oranges, 2.00)
        catalog.addProduct(apples, 1.99)
        catalog.addProduct(flour, 2.50)
    }
}
