# MIT 6.005 PS1: Tweet tweet 实验总结报告

## 1. 项目愿景 (Project Vision)
本项目是 MIT 6.005 (Software Construction) 的问题集1的实现。它不仅要求实现一个社交网络分析工具，更核心的目的是训练 **“测试优先(Test-first programming)** 的意识，以及对 **划分输入空间**和**设计测试套件** 的操作能力。通过处理真实的推文数据，我们建立起从抽象规格到具体实现的桥梁。

* **GitHub Repository**: [allwayso/ps1](https://github.com/allwayso/ps1)
* **课程参考**: [MIT 6.005 Problem Set 1](https://ocw.mit.edu/ans7870/6/6.005/s16/psets/ps1/)

---

## 2. 核心算法设计与深度逻辑解析 (Core Implementation & Logic)

在 `allwayso/ps1` 的实现过程中，我针对三个核心模块进行了深度的逻辑构建和边界处理：

### 2.1 文本挖掘：Extract 模块的鲁棒性设计
在 `getMentionedUsers` 的实现中，不仅仅是简单的字符串匹配，而是涉及到了复杂的 **字符级状态扫描**：
* **Case-Insensitivity（大小写不敏感）**：根据 Spec 要求，所有的用户名在存入 `Set` 之前必须统一转化为小写。这避免了同一用户因大小写差异被多次计算。
* **边界界定 (Valid Character Handling)**：通过自定义 `isValidChar` 方法，精确识别用户名的合法字符（字母、数字、下划线、连字符）。
* **上下文感知**：算法必须跳过邮箱地址（如 `foo@bar.com`）中的 `@bar`。我的实现通过检查 `@` 符号前的一个字符是否合法，巧妙地过滤了非推文提及。

### 2.2 图论实践：SocialNetwork 的图构建
`guessFollowsGraph` 模块将零散的推文证据转化为 **有向图 (Directed Graph)**：
* **数据聚合**：同一作者的多条推文提及会被合并到同一个 `Set<String>` 中。
* **自我提及过滤**：实现了关键的业务逻辑——用户不能“关注”自己。通过在添加关系前执行 `mentioned.remove(author)`，保证了图的正确性。
* **内存安全**：在构建 Map 时，采用了 `new HashSet<>(mentioned)` 的方式进行防御式拷贝，确保返回的图不会受到后续推文列表修改的影响。

### 2.3 性能优化：Influencers 的排序与 Map 操作
影响力算法不仅是计票，更是对 Java 集合框架性能的压榨：
* **频率统计陷阱**：在大数据量测试中，最初的 `ranking.get(names) + 1` 遇到了 `NullPointerException`。通过引入 `ranking.getOrDefault(lwName, 0) + 1`，保证了算法在处理数千名用户时的健壮性。
* **Lambda 排序机制**：
    
    使用 Java 8+ 的 Lambda 表达式 `(u1, u2) -> ranking.get(u2).compareTo(ranking.get(u1))` 实现了降序排列。相比 C++ 的 Lambda，我理解了 Java 闭包对变量 `effectively final` 的强制要求。

---

## 3. 数据考古：AnonOpsUnited2 案例研究
在 `Main.java` 接入 `tweets.json` 真实数据（3857 条推文）后，我们进行了一场非预期的“黑盒测试”，这极大加深了我对 **Tokenization（分词）** 的理解。

通过对排行榜第一名 `@AnonOpsUnited2` 的追踪，我们发现了以下技术细节：
* **分词逻辑的自我进化**：最初使用 `split("\\s+")` 导致无法匹配 `RT @AnonOpsUnited2:`。通过对真实数据的观察，我认识到在非结构化文本处理中，必须先进行 **Sanitization（清洗标点符号）**。
* **社交网络风暴**：数据展示了极高密度的“复读机”式转发（RT）。虽然 `@AnonOpsUnited2` 账号现已被封禁（可能由于涉及暴力合规或协同性造假），但我们的算法通过历史证据成功还原了他在 2023 年 4 月 18 日作为社交网络 **Hotspot** 的客观事实。



---

## 4. 实验总结 (Reflection)
通过本次 Problem Set，我对 MIT 6.005 的核心理念有了全新的认识：

1.  **Safe from bugs**：通过防御式拷贝和类型检查，确保代码在海量乱序数据下依然稳定。
2.  **Easy to understand**：代码的清晰度（Readable）与正确性同等重要，Spec 是程序员之间的契约。
3.  **Ready for change**：当数据源从失效的 `.py` 脚本切换到静态 `.json` 时，良好的模块解耦让系统表现出了极强的适应力。

**“软件构造不仅是让程序运行，更是要在数据的混沌中建立健壮的秩序。”**
