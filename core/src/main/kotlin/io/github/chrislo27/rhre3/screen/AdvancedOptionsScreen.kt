package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.analytics.AnalyticsHandler
import io.github.chrislo27.rhre3.modding.ModdingGame
import io.github.chrislo27.rhre3.modding.ModdingUtils
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.stage.TrueCheckbox
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.TextLabel


class AdvancedOptionsScreen(main: RHRE3Application) : ToolboksScreen<RHRE3Application, AdvancedOptionsScreen>(main) {

    override val stage: GenericStage<AdvancedOptionsScreen>
    private val preferences: Preferences
        get() = main.preferences
    private var didChangeSettings: Boolean = false

    private val moddingGameLabel: TextLabel<AdvancedOptionsScreen>
    private val moddingGameWarningLabel: TextLabel<AdvancedOptionsScreen>
    private var seconds = 0f

    init {
        val palette = main.uiPalette
        stage = GenericStage(main.uiPalette, null, main.defaultCamera)

        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_adv_opts"))
        stage.titleLabel.isLocalizationKey = false
        stage.titleLabel.text = "Advanced Options"
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            main.screen = ScreenRegistry.getNonNull("info")
        }

        val bottom = stage.bottomStage
        // Advanced Options setting
        bottom.elements += object : TrueCheckbox<AdvancedOptionsScreen>(palette, bottom, bottom) {
            override val checkLabelPortion: Float = 0.225f
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                preferences.putBoolean(PreferenceKeys.SETTINGS_ADVANCED_OPTIONS, checked).flush()
                main.advancedOptions = checked
                didChangeSettings = true
            }
        }.apply {
            this.checked = preferences.getBoolean(PreferenceKeys.SETTINGS_ADVANCED_OPTIONS, false)

            this.checkLabel.location.set(screenWidth = checkLabelPortion)
            this.textLabel.location.set(screenX = checkLabelPortion * 1.25f, screenWidth = 1f - checkLabelPortion * 1.25f)

            this.textLabel.apply {
//                this.fontScaleMultiplier = fontScale
                this.isLocalizationKey = false
                this.textWrapping = false
                this.textAlign = Align.left
                this.text = "Advanced Options Enabled"
            }

            this.location.set(screenX = 0.15f, screenWidth = 0.7f)
        }

        val centre = stage.centreStage
        val padding = 0.025f
        val buttonWidth = 0.4f
        val buttonHeight = 0.1f
        val fontScale = 0.75f

        moddingGameLabel = TextLabel(palette, centre, centre).apply {
            this.isLocalizationKey = false
            this.text = ""
            this.textWrapping = false
            this.fontScaleMultiplier = 0.85f
            this.textAlign = Align.top or Align.center
            this.location.set(screenX = padding,
                              screenY = padding,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight * 3 + padding * 2)
        }
        centre.elements += moddingGameLabel
        moddingGameWarningLabel = TextLabel(palette, centre, centre).apply {
            this.isLocalizationKey = false
            this.text = ""
            this.textWrapping = false
            this.fontScaleMultiplier = 0.85f
            this.textAlign = Align.top or Align.center
            this.location.set(screenX = padding,
                              screenY = padding * 4 + buttonHeight * 3,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight * 2 + padding)
        }
        centre.elements += moddingGameWarningLabel
        // Modding game reference
        centre.elements += object : Button<AdvancedOptionsScreen>(palette, centre, centre) {
            private fun updateText() {
                val game = ModdingUtils.currentGame
                val underdeveloped = game.underdeveloped
                textLabel.text = "[LIGHT_GRAY]Modding utilities with reference to:[]\n${if (underdeveloped) "[ORANGE]" else ""}${game.fullName}${if (underdeveloped) "[]" else ""}"
                updateModdingLabel()
            }

            private fun persist() {
                ModdingUtils.currentGame = ModdingGame.VALUES[index]
                preferences.putString(PreferenceKeys.ADVOPT_REF_RH_GAME, ModdingUtils.currentGame.id).flush()
                didChangeSettings = true
            }

            private var index: Int = run {
                val default = ModdingGame.DEFAULT_GAME
                val pref = preferences.getString(PreferenceKeys.ADVOPT_REF_RH_GAME, default.id)
                val values = ModdingGame.VALUES
                values.indexOf(values.find { it.id == pref } ?: default).coerceIn(0, values.size - 1)
            }

            private val textLabel: TextLabel<AdvancedOptionsScreen>
                get() = labels.first() as TextLabel

            override fun render(screen: AdvancedOptionsScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                if (textLabel.text.isEmpty()) {
                    updateText()
                }
                super.render(screen, batch, shapeRenderer)
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                index++
                if (index >= ModdingGame.VALUES.size)
                    index = 0

                persist()
                updateText()
            }

            override fun onRightClick(xPercent: Float, yPercent: Float) {
                super.onRightClick(xPercent, yPercent)
                index--
                if (index < 0)
                    index = ModdingGame.VALUES.size - 1

                persist()
                updateText()
            }

            init {
                Localization.listeners += {
                    updateText()
                }
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = false
                this.text = ""
                this.textWrapping = false
                this.fontScaleMultiplier = 0.8f
            })

            this.location.set(screenX = padding,
                              screenY = padding * 6 + buttonHeight * 5,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight * 2 + padding)
        }

        updateModdingLabel()
    }

    private fun updateModdingLabel() {
        val game = ModdingUtils.currentGame
        moddingGameWarningLabel.text = "[LIGHT_GRAY]${if (game.underdeveloped)
            "[ORANGE]Warning:[] modding info for this game\nis very underdeveloped and may be\nextremely lacking in info or incorrect."
        else
            "[YELLOW]Caution:[] modding info for this game\nmay only be partially complete and\nsubject to change."}[]\n"
        moddingGameLabel.text = "1 ♩ (quarter note) = ${game.beatsToTickflowString(1f)}${if (game.tickflowUnitName.isEmpty()) " rest units" else ""}"
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

    override fun renderUpdate() {
        super.renderUpdate()

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && stage.backButton.visible && stage.backButton.enabled) {
            stage.onBackButtonClick()
        }

        seconds += Gdx.graphics.deltaTime * 2.5f
        stage.titleIcon.rotation = (MathUtils.sin(MathUtils.sin(seconds * 2)) + seconds * 0.4f) * -90f
    }

    override fun hide() {
        super.hide()

        // Analytics
        if (didChangeSettings) {
            val map: Map<String, *> = preferences.get()
            AnalyticsHandler.track("Exit Advanced Options",
                                   mapOf(
                                           "settings" to PreferenceKeys.allAdvOptsKeys.associate {
                                               it.replace("advOpt_", "") to (map[it] ?: "null")
                                           } + ("advancedOptions" to map[PreferenceKeys.SETTINGS_ADVANCED_OPTIONS])
                                        ))
        }

        didChangeSettings = false
    }

    override fun show() {
        super.show()
        seconds = 0f
    }
}