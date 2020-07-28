package supermarket

import supermarket.model.Discount
import supermarket.model.ProductUnit
import supermarket.model.Receipt
import supermarket.model.ReceiptItem
import java.util.*

class ReceiptPrinter @JvmOverloads constructor(private val columns: Int = 40) {

    private fun Discount.priceString() = priceAsString(-discountAmount)
    private fun Discount.receiptLine() = getLRString(description, priceString())

    fun printReceipt(receipt: Receipt): String {
        val result = StringBuilder()
        with(receipt) {
            getItems().forEach { result.append(getLineForItem(it)) }
            getDiscounts().forEach { result.append(it.receiptLine()) }
        }
        result.append("\n")
        result.append(getLRString("Total: ", priceAsString(receipt.totalPrice)))
        return result.toString()
    }

    private fun getLineForItem(item: ReceiptItem): String {
        with(item) {
            var line = getLRString(product.name, priceAsString(totalPrice))

            if (quantity != 1.0) {
                line += "  ${priceAsString(price)} * ${presentQuantity(item)}\n"
            }
            return line
        }
    }

    private fun priceAsString(price: Double) = String.format(Locale.UK, "%.2f", price)

    private fun getLRString(left: String, right: String): String {
        val whitespaceSize = this.columns - left.length - right.length
        return left + " ".repeat(whitespaceSize) + right + "\n"
    }

    private fun presentQuantity(item: ReceiptItem): String =
        item.run {
            if (ProductUnit.Each == product.unit)
                String.format("%x", quantity.toInt())
            else
                String.format(Locale.UK, "%.3f", quantity)
        }
}
