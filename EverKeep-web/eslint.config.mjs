import antfu from '@antfu/eslint-config'

export default antfu({
  rules: {
    "import/no-unresolved": ["error", { "ignore": ["^virtual:"] }]
  },
})
