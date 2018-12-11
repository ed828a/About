package com.dew.ed828.aihuaPlayer.player.resolver

/**
 *
 * Created by Edward on 12/6/2018.
 *
 */

interface Resolver<Source, Product> {
    fun resolve(source: Source): Product?
}
