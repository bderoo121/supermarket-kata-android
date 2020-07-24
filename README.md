# The Supermarket Receipt Refactoring Kata

(adapted from https://github.com/emilybache/SupermarketReceipt-Refactoring-Kata)

This is a variation of a popular kata described in http://codekata.com/kata/kata01-supermarket-pricing/.
The aim of the exercise is to build automated tests for this code, refactor it, and add a new feature.  Although this
is technical an Android project, you won't see any Activities, etc - we're just using the structure, and are only
interested in running unit tests inside Android studio.

The supermarket has a catalog with different types of products (rice, apples, milk, toothbrushes,...). Each product
has a price, and the total price of the shopping cart is the total of all the prices of the items. You get a receipt
that details the items you've bought, the total price and any discounts that were applied.

The supermarket runs special deals, e.g.
 - Buy two toothbrushes, get one free. Normal toothbrush price is €0.99
 - 20% discount on apples, normal price €1.99 per kilo.
 - 10% discount on rice, normal price €2.49 per bag
 - Five tubes of toothpaste for €7.49, normal price €1.79
 - Two boxes of cherry tomatoes for €0.99, normal price €0.69 per box.

These are just examples: the actual special deals change each week.

Create some test cases and aim to get good enough code coverage that you feel confident to do some refactoring.  (NOTE:
the existing test seems to get pretty good coverage numbers, but has no assertions!  Just a reminder that coverage
stats alone aren't everything :) )

When you have good test cases, identify code smells such as Long Method, Feature Envy.  Think about ways the code could
be cleaner, easier to understand, more extensible.  Apply relevant refactorings, and make sure your tests still work
(or are updated to match your new structures).

When you're confident you can handle this code, implement the new feature described below

## New feature: discounted bundles

The owner of the system has a new feature request. They want to introduce a new kind of special offer - bundles. When
you buy all the items in a product bundle you get 10% off the total for those items. For example you could make a bundle
offer of one toothbrush and one toothpaste. If you then you buy one toothbrush and one toothpaste, the discount will be
10% of €0.99 + €1.79. If you instead buy two toothbrushes and one toothpaste, you get the same discount as if you'd
bought only one of each - ie only complete bundles are discounted.

## New feature: HTML receipt

Currently we print a traditional ticket receipt. Now being a modern business we'd
like to be able to print or send an HTML version of the same receipt. All the data
and number formatting should be the same. However the layout should be HTML.
You don't have to worry about the HTML template - a designer will care of that - but
we do need a way to keep duplication between the reports to a bare minimum.
