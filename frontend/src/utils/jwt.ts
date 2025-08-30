/**
 * JWT utility functions for decoding tokens and extracting user information
 */

export interface JwtPayload {
  sub: string // User ID
  iat: number // Issued at
  exp: number // Expiration time
}

/**
 * Decode a JWT token and return the payload
 * @param token The JWT token to decode
 * @returns The decoded payload or null if invalid
 */
export function decodeJwt(token: string): JwtPayload | null {
  try {
    // JWT tokens have 3 parts separated by dots
    const parts = token.split('.')
    if (parts.length !== 3) {
      return null
    }

    // Decode the payload (second part)
    const payload = parts[1]
    // Add padding if needed for base64 decoding
    const paddedPayload = payload + '='.repeat((4 - payload.length % 4) % 4)
    const decodedPayload = atob(paddedPayload.replace(/-/g, '+').replace(/_/g, '/'))
    
    return JSON.parse(decodedPayload)
  } catch (error) {
    console.error('Error decoding JWT:', error)
    return null
  }
}

/**
 * Extract user ID from JWT token
 * @param token The JWT token
 * @returns The user ID as a number, or null if invalid
 */
export function getUserIdFromToken(token: string): number | null {
  const payload = decodeJwt(token)
  if (!payload || !payload.sub) {
    return null
  }
  
  const userId = parseInt(payload.sub, 10)
  return isNaN(userId) ? null : userId
}

/**
 * Check if a JWT token is expired
 * @param token The JWT token
 * @returns True if expired, false otherwise
 */
export function isTokenExpired(token: string): boolean {
  const payload = decodeJwt(token)
  if (!payload || !payload.exp) {
    return true
  }
  
  const currentTime = Math.floor(Date.now() / 1000)
  return payload.exp < currentTime
}

/**
 * Get token expiration time
 * @param token The JWT token
 * @returns Expiration time as Date object, or null if invalid
 */
export function getTokenExpiration(token: string): Date | null {
  const payload = decodeJwt(token)
  if (!payload || !payload.exp) {
    return null
  }
  
  return new Date(payload.exp * 1000)
}
