package io.violabs.konstellation.dsl.utils

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class KDocUtilsTest {
    
    @Test
    fun `cleanKDoc should return null for null input`() {
        assertNull(KDocUtils.cleanKDoc(null))
    }
    
    @Test
    fun `cleanKDoc should return null for blank input`() {
        assertNull(KDocUtils.cleanKDoc(""))
        assertNull(KDocUtils.cleanKDoc("   "))
        assertNull(KDocUtils.cleanKDoc("\n\n"))
    }
    
    @Test
    fun `cleanKDoc should clean single line KDoc`() {
        val rawDoc = "\n * The name of the starship.\n"
        val expected = "The name of the starship."
        assertEquals(expected, KDocUtils.cleanKDoc(rawDoc))
    }
    
    @Test
    fun `cleanKDoc should clean multi-line KDoc`() {
        val rawDoc = """
            
             * List of commander names for this starship.
             * Each commander has authority over the vessel.
            
        """.trimIndent()
        
        val expected = "List of commander names for this starship. Each commander has authority over the vessel."
        assertEquals(expected, KDocUtils.cleanKDoc(rawDoc))
    }
    
    @Test
    fun `cleanKDoc should handle KDoc with asterisks and various formatting`() {
        val rawDoc = """
            
             * Map of crew members by their ID.
             * 
             * This includes both active and reserve crew.
            
        """.trimIndent()
        
        val expected = "Map of crew members by their ID. This includes both active and reserve crew."
        assertEquals(expected, KDocUtils.cleanKDoc(rawDoc))
    }
    
    @Test
    fun `formatForGeneration should return null for null input`() {
        assertNull(KDocUtils.formatForGeneration(null))
    }
    
    @Test
    fun `formatForGeneration should return null for blank input`() {
        assertNull(KDocUtils.formatForGeneration(""))
        assertNull(KDocUtils.formatForGeneration("   "))
    }
    
    @Test
    fun `formatForGeneration should format simple documentation`() {
        val cleanedDoc = "The name of the starship."
        val expected = """/**
    * The name of the starship.
    */"""
        assertEquals(expected, KDocUtils.formatForGeneration(cleanedDoc))
    }
    
    @Test
    fun `formatForGeneration should format with custom indent`() {
        val cleanedDoc = "The name of the starship."
        val expected = """/**
  * The name of the starship.
  */"""
        assertEquals(expected, KDocUtils.formatForGeneration(cleanedDoc, "  "))
    }
    
    @Test
    fun `full workflow should clean and format KDoc correctly`() {
        val rawDoc = "\n * The name of the starship.\n"
        val cleaned = KDocUtils.cleanKDoc(rawDoc)
        val formatted = KDocUtils.formatForGeneration(cleaned)
        
        val expected = """/**
    * The name of the starship.
    */"""
        assertEquals(expected, formatted)
    }
    
    @Test
    fun `prepareForKotlinPoet should return cleaned text for KotlinPoet addKdoc`() {
        val rawDoc = "\n * The name of the starship.\n"
        val expected = "The name of the starship."
        assertEquals(expected, KDocUtils.prepareForKotlinPoet(rawDoc))
    }
    
    @Test
    fun `prepareForKotlinPoet should return null for null input`() {
        assertNull(KDocUtils.prepareForKotlinPoet(null))
    }
}
