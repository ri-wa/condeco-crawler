package services
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.devtools.v131.network.Network
import org.openqa.selenium.support.ui.FluentWait
import java.time.Duration
import java.util.*
import java.util.function.Consumer
import java.util.logging.Logger


class TokenService(private val baseUrl: String, private val username: String, private val password: String) {

    private val usernameFieldId = "txtUserName"
    private val passwordFieldId = "txtPassword"
    private val maxTimeout = Duration.ofSeconds(10)
    private val pollingFrequency = Duration.ofMillis(500)
    private val logger: Logger = Logger.getLogger(TokenService::class.java.name)
    fun getTokens(): List<String> {
        logger.info("Getting tokens for : $username")
        var bearerToken = ""
        var accessToken = ""

        val options = ChromeOptions()
        options.addArguments("--headless")
        options.addArguments("--no-sandbox")
        options.addArguments("--disable-gpu")
        val driver = ChromeDriver(options)
        val devTools = driver.devTools
        devTools.createSession()
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()))

        devTools.addListener(Network.requestWillBeSent(), Consumer {
            if (it.documentURL.contains("enterpriselite/auth")) {
                val entries = it.request.postData
                if (entries != null) {
                    bearerToken = entries.get().split("&").first().split("=").last()
                }
            }
        })
        devTools.addListener(Network.responseReceived(), Consumer {
            if (it.response.url.contains("GetAppSetting")) {
                accessToken = it.response.url.split("?").last().split("=").last()
            }
        })
        val wait = FluentWait(driver).withTimeout(maxTimeout).pollingEvery(pollingFrequency).ignoring(NoSuchElementException::class.java)

        driver.get(baseUrl)
        wait.until { driver.findElement(By.id(usernameFieldId)) }
        enterText(driver, usernameFieldId, username)
        enterText(driver, passwordFieldId, password)

        driver.findElement(By.id("btnLogin")).click()
        wait.until { driver.findElement(By.id("CondecoTopBar")) }
        driver.navigate().to(baseUrl.plus("/EnterpriseLiteLogin.aspx?useradmin=2&amp;showProfile=1"))
        wait.until { driver.findElement(By.className("appUserAdmin")) }
        driver.quit()
        return listOf(bearerToken, accessToken)
    }

    private fun enterText(driver: ChromeDriver, id: String, value: String){
        val field = driver.findElement(By.id(id))
        field.click()
        field.sendKeys(value)
    }
}