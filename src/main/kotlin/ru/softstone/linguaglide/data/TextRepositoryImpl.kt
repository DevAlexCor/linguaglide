package ru.softstone.linguaglide.data

import ru.softstone.linguaglide.domain.repository.TextRepository

class TextRepositoryImpl : TextRepository {
    private var text: String = ""

    override fun setText(text: String) {
        this.text = text
    }

    override fun getText(): String {
        return text
    }
}