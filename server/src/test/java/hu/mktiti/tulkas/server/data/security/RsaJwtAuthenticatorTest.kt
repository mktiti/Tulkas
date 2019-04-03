package hu.mktiti.tulkas.server.data.security

import com.auth0.jwt.algorithms.Algorithm
import org.junit.jupiter.api.Test
import sun.security.rsa.RSAPublicKeyImpl
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class RsaJwtAuthenticatorTest {

    private val publicKeyString = """
        MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnzyis1ZjfNB0bBgKFMSv
        vkTtwlvBsaJq7S5wA+kzeVOVpVWwkWdVha4s38XM/pa/yr47av7+z3VTmvDRyAHc
        aT92whREFpLv9cj5lTeJSibyr/Mrm/YtjCZVWgaOYIhwrXwKLqPr/11inWsAkfIy
        tvHWTxZYEcXLgAXFuUuaS3uF9gEiNQwzGTU1v0FqkqTBr4B8nW3HCN47XUu0t8Y0
        e+lf4s4OxQawWD79J9/5d3Ry0vbV3Am1FtGJiJvOwRsIfVChDpYStTcHTCMqtvWb
        V6L11BWkpzGXSW4Hv43qa+GSYOD2QU68Mb59oSk2OB+BtOLpJofmbGEGgvmwyCI9
        MwIDAQAB
    """.trimIndent().replace("\n", "")

    private val privateKeyString = """
        MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCfPKKzVmN80HRsGAoUxK++RO3C
        W8GxomrtLnAD6TN5U5WlVbCRZ1WFrizfxcz+lr/Kvjtq/v7PdVOa8NHIAdxpP3bCFEQWku/1yPmV
        N4lKJvKv8yub9i2MJlVaBo5giHCtfAouo+v/XWKdawCR8jK28dZPFlgRxcuABcW5S5pLe4X2ASI1
        DDMZNTW/QWqSpMGvgHydbccI3jtdS7S3xjR76V/izg7FBrBYPv0n3/l3dHLS9tXcCbUW0YmIm87B
        Gwh9UKEOlhK1NwdMIyq29ZtXovXUFaSnMZdJbge/jepr4ZJg4PZBTrwxvn2hKTY4H4G04ukmh+Zs
        YQaC+bDIIj0zAgMBAAECggEAKIBGrbCSW2O1yOyQW9nvDUkA5EdsS58Q7US7bvM4iWpuDIBwCXur
        7/VuKnhn/HUhURLzj/JNozynSChqYyG+CvL+ZLy82LUE3ZIBkSdv/vFLFt+VvvRtf1EcsmoqenkZ
        l7aN7HD7DJeXBoz5tyVQKuH17WW0fsi9StGtCcUl+H6KzV9Gif0Kj0uLQbCg3THRvKuueBTwCTdj
        oP0PwaNADgSWb3hJPeLMm/yII4tIMGbOw+xd9wJRl+ZN9nkNtQMxszFGdKjedB6goYLQuP0WRZx+
        YtykaVJdM75bDUvsQar49Pc21Fp7UVk/CN11DX/hX3TmTJAUtqYADliVKkTbCQKBgQDLU48tBxm3
        g1CdDM/PZIEmpA3Y/m7e9eX7M1Uo/zDh4G/S9a4kkX6GQY2dLFdCtOS8M4hR11Io7MceBKDidjor
        TZ5zJPQ8+b9Rm+1GlaucGNwRW0cQk2ltT2ksPmJnQn2xvM9T8vE+a4A/YGzwmZOfpoVGykWs/tbS
        zU2aTaOybQKBgQDIfRf6OmirGPh59l+RSuDkZtISF/51mCV/S1M4DltWDwhjC2Y2T+meIsb/Mjtz
        4aVNz0EHB8yvn0TMGr94Uwjv4uBdpVSwz+xLhHL7J4rpInH+i0gxa0N+rGwsPwI8wJG95wLY+Kni
        5KCuXQw55uX1cqnnsahpRZFZEerBXhjqHwKBgBmEjiaHipm2eEqNjhMoOPFBi59dJ0sCL2/cXGa9
        yEPA6Cfgv49FV0zAM2azZuwvSbm4+fXTgTMzrDW/PPXPArPmlOk8jQ6OBY3XdOrz48q+b/gZrYyO
        A6A9ZCSyW6U7+gxxds/BYLeFxF2v21xC2f0iZ/2faykv/oQMUh34en/tAoGACqVZ2JexZyR0TUWf
        3X80YexzyzIq+OOTWicNzDQ29WLm9xtr2gZ0SUlfd72bGpQoyvDuawkm/UxfwtbIxALkvpg1gcN9
        s8XWrkviLyPyZF7H3tRWiQlBFEDjnZXa8I7pLkROCmdp3fp17cxTEeAI5feovfzZDH39MdWZuZrd
        h9ECgYBTEv8S7nK8wrxIC390kroV52eBwzckQU2mWa0thUtaGQiU1EYPCSDcjkrLXwB72ft0dW57
        KyWtvrB6rt1ORgOLeI5hFbwdGQhCHTrAR1vG3SyFPMAm+8JB+sGOD/fvjtZKx//MFNweKFNEF0C/
        o6Z2FXj90PlgF8sCQut36ZfuIQ==
    """.trimIndent().replace("\n", "")

    private val algorithm: Algorithm

    private val defaultAuth: JwtAuthenticator = RsaJwtAuthenticator()
    private val testAlgorithmAuth: JwtAuthenticator

    init {
        val keyFactory = KeyFactory.getInstance("RSA")
        val privateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyString)))
        val publicKey = RSAPublicKeyImpl(Base64.getDecoder().decode(publicKeyString))

        algorithm = Algorithm.RSA512(publicKey as RSAPublicKey, privateKey as RSAPrivateKey)
        testAlgorithmAuth = RsaJwtAuthenticator(algorithm, LocalDateTime.parse("2010-01-01T00:01"))
    }

    @Test
    fun `test verify no input`() {
        assertNull(defaultAuth.verify(""))
    }

    @Test
    fun `test verify whitespace input`() {
        assertNull(defaultAuth.verify("\t"))
    }

    @Test
    fun `test verify bad format input`() {
        assertNull(defaultAuth.verify("ASDFsdfasdfSADGsad51651dsaf"))
    }

    @Test
    fun `test verify bad algorithm`() {
        val input = """
            eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VybmFtZSIsInJvbGUi
            OiJVU0VSIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE1MTYyNDkwMjJ9.EQDNMBScu05X
            QwVs86mw2h8KgNqmGvoEvAGe9XFsJoXQbzmM1MJlkkYPpJOpO-dX1MK5fPbzHK4rnSjpx
            nrnxG2TDVpvuiRyYZ320N4lhjpxAPMayAo8bPS_DMeSsg76yGGuvcsSctTEHXryjcA5VL
            AhUh3dPR3Fp_MuK5Kh5wAXAdkKXRmAUtCl-ItqI-QIHysMRrzAmrnNs6vEO4Idj5_rChk
            fSdXUokiZlv3SuE_oDSmrCu6YpEIkwVWnQiuSuluN-wT19YxE1lX2FcDDh89QIaa8n9l2
            LpjzJBaCfTmTTSXbMF42A4BQ1qT_ytY1EojwdfP6W5ADKu7quaNcfg
        """.trimIndent().replace("\n", "")

        assertNull(testAlgorithmAuth.verify(input))
    }

    @Test
    fun `test verify bad type`() {
        val input = """
            eyJhbGciOiJSUzUxMiIsInR5cCI6IlRXSiJ9.eyJzdWIiOiJ1c2VybmFtZSIsInJvbGUi
            OiJVU0VSIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE1MTYyNDkwMjJ9.HxQlB8zup2qm
            R66caCgd4aaI58smL5oVi6GwD8zxNYRaJ45TY40Dbia0AaNo86lTrnkaesVs7OdKxdoDs
            bKpzjJY9ge2jM3DV3T6qg6OVYKiglLUM4ZW4w8s0oCsNR2Sg6Q_P8-kbcNz3H5pxI_1x3
            jg8dIzkahxVkxfadxPwJ3Nanq1CR-92OyLd1loLJNzWe41YepR8G5mEIljlbOL3G0eOnF
            eDRUsHXMIbYY6gJGMJoXnK1C-OCrml0_iCozfTgGgJ2MtiDkRD-5yX4YI8r4GIFxPg_Tq
            xkGSKo4I7q1BnWtE6KISBoCMp2JRo03WlhZVa4i5HRUxhvFuZNA5Mg
        """.trimIndent().replace("\n", "")

        assertNull(testAlgorithmAuth.verify(input))
    }

    @Test
    fun `test verify missing username`() {
        val input = """
            eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiVVNFUiIsImlhdCI6MTUx
            NjIzOTAyMiwiZXhwIjoxNTE2MjQ5MDIyfQ.Zv4Fprvjr8HoEuY1lDoAl2_CBqg2Z_EO5q
            RZIxjp9FcPnBzScmNsKNUWRIQKBTapDSzDe35P-wndLN2uxSmJKwHXVMq-ZZufzdYH-jb
            zN9RCAXnqVSEpz8HbBx8PUW_CUOZEifkQ5Kutuf4XyGifiZncWe7tbHfL6Uv5Tty55nMe
            x5tpWhoSz7GyNgQH7ItrFNT9iddUs5PsoOBjsCKh1p5AP9fiX8F98sNi47l1yXGfRAf-o
            UdDECdUNp4hmEirhBDBCCRi5Q7R1vaKiwCcqdSHXo_83Ayi-LGtpFDCVpQIcdQa3kaVZc
            CXMya9UUoxefAVKhZxl5mfe9y_f6j7yw
        """.trimIndent().replace("\n", "")

        assertNull(testAlgorithmAuth.verify(input))
    }

    @Test
    fun `test verify missing role`() {
        val input = """
            eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VybmFtZSIsImlhdCI6
            MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjQ5MDIyfQ.DpCSJ0BE0VsO-S4ur-Xau0nps_sNLa
            T2kovrQvFTCqUuhfGHy5Bg29Y2y2g5KxndAPhTBpBQTIvuYvs2lvRc-AlMKJIeMVW1uu7
            o_mbXDGHZZvA4JQ-y2mflKefLDtar-AppjXkTdCjcXdxqqdjFShn7JZ_tfxibkXm6jE94
            817NxvdD7wLbDmXhz_mwcEauzNYV7KdnRk8JRYZkEyMu9cND-176NADTjjhp5TCewCm4C
            hvzTF6H2OgrlxNX33ND2fJPkoXRei11XVcVMW5KBfU9I-jYFnRuGv0R-JwJJuyMEeyV0b
            7uPivrNQsxIZXFXaBiEkrlnIuBPh-T7rCxEg
        """.trimIndent().replace("\n", "")

        assertNull(testAlgorithmAuth.verify(input))
    }

    @Test
    fun `test verify unknown role`() {
        val input = """
            eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VybmFtZSIsInJvbGUi
            OiJVTktOT1dOIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE1MTYyNDkwMjJ9.jOcuGgwY
            4oOcHA_XCmYQHF8ZN58tA1VhlHqu-O3r8tW7VBWueVg-WYsVnRr3k1zoC1JFV3bViLlBo
            ZzgPxZd_AjNq8c28LZRjj0t0YXRU4a15ABagX2SkrvzymLB5kHT051SIOv5cbRYXu8lmT
            DH2g20A3cwO-GRNHN0WoJxZGocjxridCmRAQYV8tII0Hl4kPuLH9HLbJ_OQyjAnqXnvxy
            lNRQFay8ZIP7w-i8wY7dl_ccwenN5bfpH6ePneQ0cjCucpIgmyxAl76wYht_bXoP1IKhF
            irEojpw0QQbJkm6F2gySLk7ZP_wlfUfDtSFb6wsFWd3zL_eLCxVell7N3w
        """.trimIndent().replace("\n", "")

        assertNull(testAlgorithmAuth.verify(input))
    }

    @Test
    fun `test verify missing expiration date`() {
        val input = """
            eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VybmFtZSIsInJvbGUi
            OiJVU0VSIiwiaWF0IjoxNTE2MjM5MDIyfQ.h5ycT9BnbooP7A0im63SDgvE_6qpMVa64i
            l7Xc6XZxdSFvnVvFBTyXNgAHV0hdBIsAnLolQNQ21inNP8ltchjcza58xdh155yGGFAwU
            Ty1T5S3mRWmGbdZPVjO5iQdCt_efdyxucPr2rSB6dUtFEnhbdWwDdmvwAGml3sOkcqjbZ
            v_xTK8rmszNthjkYUi7Y-cLViMLvfxcuOLaiZ9_NM2BIFlgG1lNcgLauT8a_AAluOC3y3
            Blb6QkU2GlvdviRnzfupukHHnr_0vhvKZnl9cQ6JFsnW7Rc0VqgBsqCu2NAtkGVHMbk-N
            WWGjip_lostmr2kIxMdBdlxP8KwQHdyw
        """.trimIndent().replace("\n", "")

        assertNull(testAlgorithmAuth.verify(input))
    }

    @Test
    fun `test verify expired`() {
        val input = """
            eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VybmFtZSIsInJvbGUi
            OiJVU0VSIiwiaWF0IjoxNTEwMjM5MDIyLCJleHAiOjE1MTEyNDkwMjJ9.TOPdNDgCzWXW
            pO8HUUotMcmKqaodU40dv7UQ9V21VSEZHqd7Bk4DoAq3l3BUBw4M-cgR3qfdVa7iKtrQL
            bV0hVi3BcIZAprtnlLY6zjs9tcJFbnvoFL5cFagGZxaSzfAO-_Sjmhqr-NMcHc88BvSTX
            fYEdD16sBQRb1qAF2TrF4JeYClpERU14abIE5OX1IrIsjAGZM4gM8bJ2FIzlZO3YdQvSS
            a6nRR3nIX99HY2kY50qxGq2qXKgG8KKLYBTQk_gKoQkcHk2Ah417kJvfCR-xWB0a28yha
            suqOH-AkCTA1LT7nKGp-O7__30RchBa9ggrwrWxFu6jd8V279j361g
        """.trimIndent()

        assertNull(testAlgorithmAuth.verify(input))
    }

    @Test
    fun `test verify old issued at date`() {
        val input = """
            eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VybmFtZSIsInJvbGUi
            OiJVU0VSIiwiaWF0IjoxMDExMjQ5MDIyLCJleHAiOjMwMTEyNDkwMjJ9.H6DLi2PnYkIX
            Mrub_s50UYv2w5F8L8Qv_zss5b-Soq8u8iEMYRzDeq3o5mgpkTSDOz9VGMFUEnFUS-kK-
            ab-eO9layOmL-eHG7KE2fd0UyJe3249-zloBkrP5lq_kNn3ctykyx--LqK3zwMe6H10JM
            01nfaXkhqnXel7SzBcahXBIsVu1Idpd2NdcqxccBCOLJUWqyWoSBkwNHy9Pj109M0ZqFj
            drx8vDvitNHz8_lYPRKyYRB2kx0MOy4efCw8rkXDSz4xq9dxuKtSh1dqg03xb3uwdyGCn
            GVWWIE-tUDB7JT1hQTT-ipLyrj8lYawgY0UsZye-R5gRJKq-a2fV0w
        """.trimIndent().replace("\n", "")

        assertNull(testAlgorithmAuth.verify(input))
    }

    @Test
    fun `test verify future issued date`() {
        val input = """
            eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VybmFtZSIsInJvbGUi
            OiJVU0VSIiwiaWF0IjoyODExMjQ5MDIyLCJleHAiOjMwMTEyNDkwMjJ9.mnb9ejGzRMKX
            xXn2UD05emVTIaSYfnkQ4zaOgcEY8lP260j285LNnVSTtvNIF_nlfmF7QwkeiSs2l7N_m
            5YzArsbEWNEIuHneRLK1gmzk8zWyrUKyb4G2NNABcyL0FmjKhvCdGuhmSxJsPSPy0Mniw
            ZMe_C0I9gA16IXFpVLEhktn8tG6jhto4mKPC60eiTx-wLklvSRM97cwJbZCLOniaC0nRy
            BK_rYwKD0SE86J0ACLETvi0KH6_6_Y1SccE-TGIEMnJm0J2MOrMGEffarx5JuaVg5QyZV
            RUh_W-JjHeJt31a8h7SktaF3aCUOlQi-YKQxkgbtYjcPWkn5fuDW-Q
        """.trimIndent().replace("\n", "")

        assertNull(testAlgorithmAuth.verify(input))
    }

    @Test
    fun `test verify success`() {
        val input = """
            eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VybmFtZSIsInJvbGUi
            OiJVU0VSIiwiaWF0IjoxNTExMjQ5MDIyLCJleHAiOjMwMTEyNDkwMjJ9.hVO1f_yPMtME
            9s4dmsVi5hFWdDViKwzx3s-_t1v_Cn3ZqFAivK29Q0NFfkZ_ExAk9stxPMDmC0hr_vM-v
            4rMyD5lp97UThOIFAL7mzzyaEzTe92qRXALWsRUZcrbhjFPnwkqiQx4ZuXwVP2i9HZ-dv
            Nv0Ff6Aub4ESSaBvlS7nrWrnvIBSj8lVuMtbGMvha0DONGLLX2mNGPC76bF8c2UTbFpQm
            41TyJkHLpixr4q8a752JPXGiwVn6YChbLhIHt_eHV9_qKltbJRpppK5ULEHovrEAlDREQ
            iCKG_YxQwAMLVxyC2jujfHy7-sA83CddoJduHT-2U3wlBzWXwuzR9g
        """.trimIndent().replace("\n", "")

        assertEquals(JwtTokenData(
                username = "username", role = UserRole.USER, expiration = LocalDateTime.parse("2065-06-03T12:03:42")
        ), testAlgorithmAuth.verify(input))
    }

}