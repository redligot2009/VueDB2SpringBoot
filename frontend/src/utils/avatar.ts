// Predefined light background colors for avatars
const avatarColors = [
  '#FFB3BA', // Light pink
  '#BAFFC9', // Light green
  '#BAE1FF', // Light blue
  '#FFFFBA', // Light yellow
  '#FFB3F7', // Light purple
  '#B3FFE6', // Light cyan
  '#FFD4B3', // Light orange
  '#E6B3FF', // Light violet
  '#B3D4FF', // Light sky blue
  '#FFE6B3', // Light peach
  '#B3FFB3', // Light mint
  '#F7B3FF', // Light magenta
]

/**
 * Generate a default avatar with user initials and random background color
 * @param username - The username to extract initials from
 * @returns SVG string for the avatar
 */
export function generateDefaultAvatar(username: string): string {
  // Extract initials (first letter of each word, max 2 letters)
  const initials = username
    .split(' ')
    .map(word => word.charAt(0).toUpperCase())
    .slice(0, 2)
    .join('')

  // Generate consistent color based on username
  const colorIndex = username.length % avatarColors.length
  const backgroundColor = avatarColors[colorIndex]

  // Create SVG avatar
  const svg = `
    <svg width="40" height="40" viewBox="0 0 40 40" xmlns="http://www.w3.org/2000/svg">
      <circle cx="20" cy="20" r="20" fill="${backgroundColor}"/>
      <text x="20" y="26" font-family="Arial, sans-serif" font-size="16" font-weight="bold" text-anchor="middle" fill="#333">
        ${initials}
      </text>
    </svg>
  `

  return `data:image/svg+xml;base64,${btoa(svg)}`
}

/**
 * Get a data URL for a default avatar
 * @param username - The username to generate avatar for
 * @returns Data URL string
 */
export function getDefaultAvatarUrl(username: string): string {
  return generateDefaultAvatar(username)
}
