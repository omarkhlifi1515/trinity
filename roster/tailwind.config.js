import trinityUIPreset from "trinity-ui/src/tailwind/preset";
export default {
	presets: [trinityUIPreset],
	content: [
		"./index.html",
		"./src/**/*.{vue,js,ts,jsx,tsx}",
		"./node_modules/trinity-ui/src/components/**/*.{vue,js,ts,jsx,tsx}",
		"../node_modules/trinity-ui/src/components/**/*.{vue,js,ts,jsx,tsx}",
	],
	theme: {},
	plugins: [],
};
