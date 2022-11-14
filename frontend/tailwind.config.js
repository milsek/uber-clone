/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        "accent": "#006D5B",
      },
      spacing: {
        "88": "22rem",
      }
    },
  },
  plugins: [],
}
