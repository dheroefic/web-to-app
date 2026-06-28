# 7 语翻译提示词(葡/西/法/德/俄/日/韩)

> **给维护者**:每个语言的提示词是独立的、自包含的。做完一个语言(如葡)再发下一个(西)给那个 AI。
> 提示词里写死了文件路径、枚举名、查找替换规则,那个 AI 不需要理解项目,照葫芦画瓢即可。
> 所有占位符 `%s`/`%d` 必须原样保留,不能改代码逻辑,只能改字符串内容。

---

## 🇵🇹 提示词 1:葡萄牙语(Portuguese)

你是一个翻译执行者。你的唯一任务是把一个 Android 应用里**现有的英文翻译**替换成**葡萄牙语(Português)**。不要做任何翻译以外的事,不要改代码逻辑,不要删东西,不要加注释。

**目标语言信息**(全程用这些标识):
- 枚举名:`PORTUGUESE`
- 资源目录名:`values-pt`
- 语言代码:`pt`

**重要前提**:代码里已经有 `AppLanguage.PORTUGUESE -> "..."` 这样的占位分支,目前填的是英文。你的任务是把那些双引号里的英文**改成葡萄牙语**。**不要新增分支,不要删分支,只改双引号里的文字内容。**

### 任务一(主战场):改 `app/src/main/java/com/webtoapp/core/i18n/Strings.kt`

这个文件有大约 6600 个这样的代码块:
```
val xxx: String get() = when (lang) {
    AppLanguage.CHINESE -> "中文"
    AppLanguage.ENGLISH -> "English text"
    AppLanguage.ARABIC -> "نص عربي"
    AppLanguage.PORTUGUESE -> "English text"   ← 这一行,把引号里的英文改成葡萄牙语
    AppLanguage.SPANISH -> "English text"
    ...其他语言...
}
```

规则:
1. 找到每一个 `AppLanguage.PORTUGUESE -> "..."`,把引号里的内容从英文翻译成葡萄牙语。参考同一块里 `AppLanguage.ENGLISH ->` 那一行的英文原文来翻译。
2. **保留所有 `%s`、`%d`、`%1$s` 这类占位符**,位置和数量不能变。例如 `"Installed %s"` 翻译成 `"Instalado %s"`。
3. 保留 `&amp;`、`&lt;`、`&gt;`、`\n`、`'''`(三引号)等转义字符不变。
4. 如果英文原文是空字符串 `""`,保持空字符串。
5. 如果某行的英文是技术性内容(如 URL、JSON、代码示例、emoji 标识符如 `"block"`、`"package"`),可以保持英文不翻译——这些是程序内部用的标识符。
6. **绝对不要改 `AppLanguage.CHINESE`、`AppLanguage.ENGLISH`、`AppLanguage.ARABIC`、`AppLanguage.SPANISH` 等其他语言的分支**,只改 `PORTUGUESE`。

### 任务二:改 `app/src/main/java/com/webtoapp/core/i18n/AiPromptManager.kt`

这个文件里有几个 `AppLanguage.PORTUGUESE ->` 分支(目前是英文 prompt)。把它们翻译成葡萄牙语。这些是 AI 编程用的提示词,内容较长,照着英文意思翻译即可,保留其中的 ` ```javascript `、` ```json `、`$attempt`、`$code` 等变量和代码块标记。

### 任务三:改 `app/src/main/java/com/webtoapp/core/i18n/RandomAppNameGenerator.kt`

这个文件有两个 `AppLanguage.PORTUGUESE -> generateEnglish()` 调用。**这一项跳过,不要改**——随机应用名生成需要专门的葡语单词表,留作以后处理。只确认这两行存在即可。

### 任务四(资源文件):新建 `app/src/main/res/values-pt/` 目录

参照 `app/src/main/res/values-en/` 目录,里面有这些文件(共 18 个):
`app_strings_ai.xml`、`app_strings_ai_coding.xml`、`app_strings_ai_config.xml`、`app_strings_build.xml`、`app_strings_common.xml`、`app_strings_compat.xml`、`app_strings_create.xml`、`app_strings_extension.xml`、`app_strings_legacy.xml`、`app_strings_module.xml`、`app_strings_music.xml`、`app_strings_project.xml`、`app_strings_sample.xml`、`app_strings_shell.xml`、`app_strings_snippet.xml`、`app_strings_ui.xml`、`app_strings_web_view.xml`、`strings.xml`。

规则:
1. 在 `app/src/main/res/values-pt/` 下**新建全部 18 个文件**,文件名和 `values-en/` 完全一致。
2. 每个文件的内容 = 对应 `values-en/` 文件的翻译版。**`<string name="xxx">` 的 name 属性必须一字不差地照抄**,只翻译标签之间的文字。
3. 保留所有 `%1$s`、`%s`、`%d`、`\n`、`&amp;` 等不变。
4. **文件数量必须正好 18 个,一个都不能少**——有个测试会检查每个语言目录的文件集合和 `values/` 完全一致。

### 完成后自检
- 在 `Strings.kt` 里搜索 `AppLanguage.PORTUGUESE ->`,确认每一行的引号里都是葡萄牙语(不再是英文)。
- 确认 `values-pt/` 目录下有 18 个文件,文件名和 `values-en/` 一致。
- 确认没有动 `CHINESE`/`ENGLISH`/`ARABIC` 等其他语言的分支。
- 确认所有 `%s`/`%d` 占位符都保留。

---

## 🇪🇸 提示词 2:西班牙语(Spanish)

你是一个翻译执行者。你的唯一任务是把一个 Android 应用里**现有的英文翻译**替换成**西班牙语(Español)**。不要做任何翻译以外的事,不要改代码逻辑,不要删东西,不要加注释。

**目标语言信息**(全程用这些标识):
- 枚举名:`SPANISH`
- 资源目录名:`values-es`
- 语言代码:`es`

**重要前提**:代码里已经有 `AppLanguage.SPANISH -> "..."` 这样的占位分支,目前填的是英文。你的任务是把那些双引号里的英文**改成西班牙语**。**不要新增分支,不要删分支,只改双引号里的文字内容。**

### 任务一(主战场):改 `app/src/main/java/com/webtoapp/core/i18n/Strings.kt`

这个文件有大约 6600 个这样的代码块:
```
val xxx: String get() = when (lang) {
    AppLanguage.CHINESE -> "中文"
    AppLanguage.ENGLISH -> "English text"
    AppLanguage.ARABIC -> "نص عربي"
    AppLanguage.PORTUGUESE -> "..."
    AppLanguage.SPANISH -> "English text"   ← 这一行,把引号里的英文改成西班牙语
    ...其他语言...
}
```

规则:
1. 找到每一个 `AppLanguage.SPANISH -> "..."`,把引号里的内容从英文翻译成西班牙语。参考同一块里 `AppLanguage.ENGLISH ->` 那一行的英文原文来翻译。
2. **保留所有 `%s`、`%d`、`%1$s` 这类占位符**,位置和数量不能变。例如 `"Installed %s"` 翻译成 `"Instalado %s"`。
3. 保留 `&amp;`、`&lt;`、`&gt;`、`\n`、`'''`(三引号)等转义字符不变。
4. 如果英文原文是空字符串 `""`,保持空字符串。
5. 如果某行的英文是技术性内容(如 URL、JSON、代码示例、emoji 标识符如 `"block"`、`"package"`),可以保持英文不翻译——这些是程序内部用的标识符。
6. **绝对不要改 `AppLanguage.CHINESE`、`AppLanguage.ENGLISH`、`AppLanguage.ARABIC`、`AppLanguage.PORTUGUESE` 等其他语言的分支**,只改 `SPANISH`。

### 任务二:改 `app/src/main/java/com/webtoapp/core/i18n/AiPromptManager.kt`

这个文件里有几个 `AppLanguage.SPANISH ->` 分支(目前是英文 prompt)。把它们翻译成西班牙语。这些是 AI 编程用的提示词,内容较长,照着英文意思翻译即可,保留其中的 ` ```javascript `、` ```json `、`$attempt`、`$code` 等变量和代码块标记。

### 任务三:改 `app/src/main/java/com/webtoapp/core/i18n/RandomAppNameGenerator.kt`

这个文件有两个 `AppLanguage.SPANISH -> generateEnglish()` 调用。**这一项跳过,不要改**——随机应用名生成需要专门的西语单词表,留作以后处理。

### 任务四(资源文件):新建 `app/src/main/res/values-es/` 目录

参照 `app/src/main/res/values-en/` 目录,里面有 18 个文件:`app_strings_ai.xml`、`app_strings_ai_coding.xml`、`app_strings_ai_config.xml`、`app_strings_build.xml`、`app_strings_common.xml`、`app_strings_compat.xml`、`app_strings_create.xml`、`app_strings_extension.xml`、`app_strings_legacy.xml`、`app_strings_module.xml`、`app_strings_music.xml`、`app_strings_project.xml`、`app_strings_sample.xml`、`app_strings_shell.xml`、`app_strings_snippet.xml`、`app_strings_ui.xml`、`app_strings_web_view.xml`、`strings.xml`。

规则:
1. 在 `app/src/main/res/values-es/` 下**新建全部 18 个文件**,文件名和 `values-en/` 完全一致。
2. 每个文件的内容 = 对应 `values-en/` 文件的翻译版。**`<string name="xxx">` 的 name 属性必须一字不差地照抄**,只翻译标签之间的文字。
3. 保留所有 `%1$s`、`%s`、`%d`、`\n`、`&amp;` 等不变。
4. **文件数量必须正好 18 个,一个都不能少**。

### 完成后自检
- 在 `Strings.kt` 里搜索 `AppLanguage.SPANISH ->`,确认每一行的引号里都是西班牙语。
- 确认 `values-es/` 目录下有 18 个文件。
- 确认没有动其他语言的分支。
- 确认所有 `%s`/`%d` 占位符都保留。

---

## 🇫🇷 提示词 3:法语(French)

你是一个翻译执行者。你的唯一任务是把一个 Android 应用里**现有的英文翻译**替换成**法语(Français)**。不要做任何翻译以外的事,不要改代码逻辑,不要删东西,不要加注释。

**目标语言信息**:
- 枚举名:`FRENCH`
- 资源目录名:`values-fr`
- 语言代码:`fr`

**重要前提**:代码里已经有 `AppLanguage.FRENCH -> "..."` 这样的占位分支,目前填的是英文。你的任务是把那些双引号里的英文**改成法语**。**不要新增分支,不要删分支,只改双引号里的文字内容。**

### 任务一(主战场):改 `app/src/main/java/com/webtoapp/core/i18n/Strings.kt`

规则:
1. 找到每一个 `AppLanguage.FRENCH -> "..."`,把引号里的内容从英文翻译成法语。参考同一块里 `AppLanguage.ENGLISH ->` 那一行的英文原文来翻译。
2. **保留所有 `%s`、`%d`、`%1$s` 这类占位符**,位置和数量不能变。例如 `"Installed %s"` 翻译成 `"Installé %s"`。
3. 保留 `&amp;`、`&lt;`、`&gt;`、`\n`、`'''`(三引号)等转义字符不变。
4. 如果英文原文是空字符串 `""`,保持空字符串。
5. 如果某行的英文是技术性内容(如 URL、JSON、代码示例、emoji 标识符如 `"block"`、`"package"`),可以保持英文不翻译。
6. **绝对不要改其他语言的分支**,只改 `FRENCH`。

### 任务二:改 `app/src/main/java/com/webtoapp/core/i18n/AiPromptManager.kt`

把里面 `AppLanguage.FRENCH ->` 分支(目前是英文 prompt)翻译成法语。保留其中的 ` ```javascript `、` ```json `、`$attempt`、`$code` 等变量和代码块标记。

### 任务三:改 `app/src/main/java/com/webtoapp/core/i18n/RandomAppNameGenerator.kt`

**跳过,不要改**(`AppLanguage.FRENCH -> generateEnglish()`)。

### 任务四(资源文件):新建 `app/src/main/res/values-fr/` 目录

参照 `values-en/` 目录,新建全部 18 个文件,文件名完全一致。`<string name="xxx">` 的 name 属性照抄,只翻译标签之间的文字。保留所有 `%1$s`、`%s`、`%d`、`\n`、`&amp;` 不变。文件数量必须正好 18 个。

### 完成后自检
- 在 `Strings.kt` 搜索 `AppLanguage.FRENCH ->`,确认每行都是法语。
- 确认 `values-fr/` 有 18 个文件。
- 确认没动其他语言分支,占位符都保留。

---

## 🇩🇪 提示词 4:德语(German)

你是一个翻译执行者。你的唯一任务是把一个 Android 应用里**现有的英文翻译**替换成**德语(Deutsch)**。不要做任何翻译以外的事,不要改代码逻辑,不要删东西,不要加注释。

**目标语言信息**:
- 枚举名:`GERMAN`
- 资源目录名:`values-de`
- 语言代码:`de`

**重要前提**:代码里已经有 `AppLanguage.GERMAN -> "..."` 这样的占位分支,目前填的是英文。你的任务是把那些双引号里的英文**改成德语**。**不要新增分支,不要删分支,只改双引号里的文字内容。**

### 任务一(主战场):改 `app/src/main/java/com/webtoapp/core/i18n/Strings.kt`

规则:
1. 找到每一个 `AppLanguage.GERMAN -> "..."`,把引号里的内容从英文翻译成德语。参考同一块里 `AppLanguage.ENGLISH ->` 那一行的英文原文来翻译。
2. **保留所有 `%s`、`%d`、`%1$s` 这类占位符**。例如 `"Installed %s"` 翻译成 `"Installiert %s"`。
3. 保留 `&amp;`、`&lt;`、`&gt;`、`\n`、`'''` 等转义字符不变。
4. 空字符串保持空字符串。
5. 技术性内容(URL、JSON、代码示例、emoji 标识符)可保持英文。
6. **绝对不要改其他语言的分支**,只改 `GERMAN`。

### 任务二:改 `app/src/main/java/com/webtoapp/core/i18n/AiPromptManager.kt`

把 `AppLanguage.GERMAN ->` 分支翻译成德语,保留代码块标记和变量。

### 任务三:改 `RandomAppNameGenerator.kt`

**跳过,不要改**。

### 任务四(资源文件):新建 `app/src/main/res/values-de/` 目录

参照 `values-en/`,新建全部 18 个文件。name 属性照抄,只翻译文字。保留占位符。文件数必须 18 个。

### 完成后自检
- `Strings.kt` 搜索 `AppLanguage.GERMAN ->`,确认每行都是德语。
- `values-de/` 有 18 个文件。
- 没动其他语言分支,占位符保留。

---

## 🇷🇺 提示词 5:俄语(Russian)

你是一个翻译执行者。你的唯一任务是把一个 Android 应用里**现有的英文翻译**替换成**俄语(Русский)**。不要做任何翻译以外的事,不要改代码逻辑,不要删东西,不要加注释。

**目标语言信息**:
- 枚举名:`RUSSIAN`
- 资源目录名:`values-ru`
- 语言代码:`ru`

**重要前提**:代码里已经有 `AppLanguage.RUSSIAN -> "..."` 这样的占位分支,目前填的是英文。你的任务是把那些双引号里的英文**改成俄语**。**不要新增分支,不要删分支,只改双引号里的文字内容。**

### 任务一(主战场):改 `app/src/main/java/com/webtoapp/core/i18n/Strings.kt`

规则:
1. 找到每一个 `AppLanguage.RUSSIAN -> "..."`,把引号里的内容从英文翻译成俄语。参考同一块里 `AppLanguage.ENGLISH ->` 那一行的英文原文来翻译。
2. **保留所有 `%s`、`%d`、`%1$s` 这类占位符**。例如 `"Installed %s"` 翻译成 `"Установлено %s"`。
3. 保留 `&amp;`、`&lt;`、`&gt;`、`\n`、`'''` 等转义字符不变。
4. 空字符串保持空字符串。
5. 技术性内容(URL、JSON、代码示例、emoji 标识符)可保持英文。
6. **绝对不要改其他语言的分支**,只改 `RUSSIAN`。

### 任务二:改 `app/src/main/java/com/webtoapp/core/i18n/AiPromptManager.kt`

把 `AppLanguage.RUSSIAN ->` 分支翻译成俄语,保留代码块标记和变量。

### 任务三:改 `RandomAppNameGenerator.kt`

**跳过,不要改**。

### 任务四(资源文件):新建 `app/src/main/res/values-ru/` 目录

参照 `values-en/`,新建全部 18 个文件。name 属性照抄,只翻译文字。保留占位符。文件数必须 18 个。

### 完成后自检
- `Strings.kt` 搜索 `AppLanguage.RUSSIAN ->`,确认每行都是俄语。
- `values-ru/` 有 18 个文件。
- 没动其他语言分支,占位符保留。

---

## 🇯🇵 提示词 6:日语(Japanese)

你是一个翻译执行者。你的唯一任务是把一个 Android 应用里**现有的英文翻译**替换成**日语(日本語)**。不要做任何翻译以外的事,不要改代码逻辑,不要删东西,不要加注释。

**目标语言信息**:
- 枚举名:`JAPANESE`
- 资源目录名:`values-ja`
- 语言代码:`ja`

**重要前提**:代码里已经有 `AppLanguage.JAPANESE -> "..."` 这样的占位分支,目前填的是英文。你的任务是把那些双引号里的英文**改成日语**。**不要新增分支,不要删分支,只改双引号里的文字内容。**

### 任务一(主战场):改 `app/src/main/java/com/webtoapp/core/i18n/Strings.kt`

规则:
1. 找到每一个 `AppLanguage.JAPANESE -> "..."`,把引号里的内容从英文翻译成日语。参考同一块里 `AppLanguage.ENGLISH ->` 那一行的英文原文来翻译。
2. **保留所有 `%s`、`%d`、`%1$s` 这类占位符**。例如 `"Installed %s"` 翻译成 `"インストール済み %s"`。
3. 保留 `&amp;`、`&lt;`、`&gt;`、`\n`、`'''` 等转义字符不变。
4. 空字符串保持空字符串。
5. 技术性内容(URL、JSON、代码示例、emoji 标识符)可保持英文。
6. **绝对不要改其他语言的分支**,只改 `JAPANESE`。

### 任务二:改 `app/src/main/java/com/webtoapp/core/i18n/AiPromptManager.kt`

把 `AppLanguage.JAPANESE ->` 分支翻译成日语,保留代码块标记和变量。

### 任务三:改 `RandomAppNameGenerator.kt`

**跳过,不要改**。

### 任务四(资源文件):新建 `app/src/main/res/values-ja/` 目录

参照 `values-en/`,新建全部 18 个文件。name 属性照抄,只翻译文字。保留占位符。文件数必须 18 个。

### 完成后自检
- `Strings.kt` 搜索 `AppLanguage.JAPANESE ->`,确认每行都是日语。
- `values-ja/` 有 18 个文件。
- 没动其他语言分支,占位符保留。

---

## 🇰🇷 提示词 7:韩语(Korean)

你是一个翻译执行者。你的唯一任务是把一个 Android 应用里**现有的英文翻译**替换成**韩语(한국어)**。不要做任何翻译以外的事,不要改代码逻辑,不要删东西,不要加注释。

**目标语言信息**:
- 枚举名:`KOREAN`
- 资源目录名:`values-ko`
- 语言代码:`ko`

**重要前提**:代码里已经有 `AppLanguage.KOREAN -> "..."` 这样的占位分支,目前填的是英文。你的任务是把那些双引号里的英文**改成韩语**。**不要新增分支,不要删分支,只改双引号里的文字内容。**

### 任务一(主战场):改 `app/src/main/java/com/webtoapp/core/i18n/Strings.kt`

规则:
1. 找到每一个 `AppLanguage.KOREAN -> "..."`,把引号里的内容从英文翻译成韩语。参考同一块里 `AppLanguage.ENGLISH ->` 那一行的英文原文来翻译。
2. **保留所有 `%s`、`%d`、`%1$s` 这类占位符**。例如 `"Installed %s"` 翻译成 `"설치됨 %s"`。
3. 保留 `&amp;`、`&lt;`、`&gt;`、`\n`、`'''` 等转义字符不变。
4. 空字符串保持空字符串。
5. 技术性内容(URL、JSON、代码示例、emoji 标识符)可保持英文。
6. **绝对不要改其他语言的分支**,只改 `KOREAN`。

### 任务二:改 `app/src/main/java/com/webtoapp/core/i18n/AiPromptManager.kt`

把 `AppLanguage.KOREAN ->` 分支翻译成韩语,保留代码块标记和变量。

### 任务三:改 `RandomAppNameGenerator.kt`

**跳过,不要改**。

### 任务四(资源文件):新建 `app/src/main/res/values-ko/` 目录

参照 `values-en/`,新建全部 18 个文件。name 属性照抄,只翻译文字。保留占位符。文件数必须 18 个。

### 完成后自检
- `Strings.kt` 搜索 `AppLanguage.KOREAN ->`,确认每行都是韩语。
- `values-ko/` 有 18 个文件。
- 没动其他语言分支,占位符保留。
