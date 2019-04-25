package com.example.in2000_project.utils

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import java.util.*


public abstract class WeatherdataUtil{

    @Root(name="weatherdata")
    data class Weatherdata(
        @field:Element(name="product", required = false) val product: Product
    )
    data class Product(
        @field:ElementList(name="time", required = false) var time: List<Time>
    )
    data class Time(
        @field:Attribute(name="from", required = false) val from: Date,
        @field:Attribute(name="to", required = false) val to: Date,
        @field:Element(name="location", required = false) val location: Location)
    data class Location(
        @field:Attribute(name="latitude", required = false) val latitude: Long,
        @field:Attribute(name="longitude", required = false) val longitude: Long,
        @field:Element(name="symbol", required = false) val symbol: Symbol
    )
    data class Symbol(
        @field:Attribute(name="id", required = false) val id: String
    )



    companion object {

    }



}