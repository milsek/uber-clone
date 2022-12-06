/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        "accent": "#006d5b",
        "accent-light": "#009B81",
        "dark": "#3d3c41",
        "medium": "#585761",
        "light": "#93959b",
      },
      spacing: {
        "88": "22rem",
        "120": "30rem",
        "132": "33rem",
        "140": "35rem",
        "180": "45rem",
        "200": "50rem",
      }
    },
  },
  plugins: [],
}
