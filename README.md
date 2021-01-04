### Android mvp 架构
- 使用 "动态代理" 简化 presenter 中的 `getView()` 操作, 在架构层避免 view 层内存泄露问题
- 支持一个 view 依赖多个 presenter, presenter 依靠 koin 注入并自动绑定 view
- 支持多模块注入
