package com.archmage.ghostmind.model

/**
  * what is an inventory?
  *
  * a miserable pile of secrets.
  *
  * no, really, what is it?
  *
  * it's a collection of items, limited by encumbrance. (server-side, anyway)
  * items are a finite set of names, weights and actions.
  *
  * some items are "stacking", in that each instance is identical to each other instance
  * others are "non-stacking", like guns and radios, where each is its own unique instance with its own additional data
  *
  * stacking items could plausibly be condensed down to a Map[ItemType -> Int]
  * non-stacking items are a bit harder
  * they could be treated as stacking
  *
  * before moving forward, architectural decisions need to be made
  */
case class Inventory() {

}
