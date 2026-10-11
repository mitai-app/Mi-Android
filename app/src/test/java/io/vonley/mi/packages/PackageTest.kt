package io.vonley.mi.packages

import io.vonley.mi.ui.screens.packages.data.local.entity.Package
import io.vonley.mi.ui.screens.packages.data.local.entity.PackageType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PackageTest {

    private fun pkg(type: PackageType, dl: Map<String, String>) =
        Package("link", "GoldHen", "SiSTRo", "2.2.4", type, "", dl)

    @Test
    fun `finds downloads keyed by v-prefixed firmware`() {
        val p = pkg(PackageType.PLUGIN, mapOf("v900" to "a", "v755" to "b"))
        assertEquals("a", p.downloadFor("9.00"))
        assertEquals("b", p.downloadFor("7.55"))
        assertNull(p.downloadFor("6.72"))
    }

    @Test
    fun `accepts plain firmware keys`() {
        assertEquals("a", pkg(PackageType.PLUGIN, mapOf("900" to "a")).downloadFor("9.00"))
    }

    @Test
    fun `tools are sent as bin payloads`() {
        assertEquals(".bin", pkg(PackageType.TOOL, emptyMap()).fileExtension)
        assertEquals(".bin", pkg(PackageType.PLUGIN, emptyMap()).fileExtension)
        assertEquals(".pkg", pkg(PackageType.APP, emptyMap()).fileExtension)
    }
}
