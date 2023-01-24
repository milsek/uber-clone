/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        // "accent": "#57cc99",
        "accent": "#ec4f0b",
        "accent-light": "#ff7035",
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
        "300": "75rem",
      },
      zIndex: {
        '100': '100',
      }
    },
  },
  plugins: [],
}
