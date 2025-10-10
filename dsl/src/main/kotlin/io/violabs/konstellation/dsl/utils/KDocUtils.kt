package io.violabs.konstellation.dsl.utils

/**
 * Utility class for processing KDoc comments from KSP.
 */
object KDocUtils {
    
    /**
     * Cleans raw KDoc content by removing formatting artifacts.
     * 
     * KSP's docString property returns the raw content between /** and */, including:
     * - Leading/trailing whitespace and newlines
     * - Leading asterisks on each line
     * - Extra spacing from formatting
     * 
     * This function processes the raw content to extract just the meaningful documentation text.
     * 
     * @param rawDocString The raw docString from KSP
     * @return Cleaned documentation text, or null if input is null/empty
     */
    fun cleanKDoc(rawDocString: String?): String? {
        if (rawDocString.isNullOrBlank()) return null
        
        return rawDocString
            .lines()
            .map { line ->
                // Remove leading whitespace and asterisk
                line.trimStart()
                    .removePrefix("*")
                    .trimStart()
            }
            .filter { it.isNotBlank() } // Remove empty lines
            .joinToString(" ") { it.trim() } // Join with single spaces
            .takeIf { it.isNotBlank() }
    }
    
    /**
     * Formats cleaned KDoc content for code generation with proper indentation.
     * This creates a complete KDoc block with /** */ delimiters.
     * 
     * @param cleanedDoc The cleaned documentation text
     * @param indent The indentation string to use (default is 4 spaces)
     * @return Formatted KDoc comment ready for code generation
     */
    fun formatForGeneration(cleanedDoc: String?, indent: String = "    "): String? {
        if (cleanedDoc.isNullOrBlank()) return null
        
        return buildString {
            appendLine("/**")
            // Split long lines if needed (optional enhancement)
            appendLine("$indent* $cleanedDoc")
            append("$indent*/")
        }
    }
    
    /**
     * Prepares cleaned KDoc content for KotlinPoet's addKdoc method.
     * KotlinPoet expects just the content, not the /** */ delimiters.
     * 
     * @param rawDocString The raw docString from KSP
     * @return Cleaned documentation text suitable for KotlinPoet's addKdoc, or null if input is null/empty
     */
    fun prepareForKotlinPoet(rawDocString: String?): String? {
        return cleanKDoc(rawDocString)
    }
}
