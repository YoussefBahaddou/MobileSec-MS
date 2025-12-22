
import { createClient } from '@supabase/supabase-js'

const supabaseUrl = process.env.REACT_APP_SUPABASE_URL
const supabaseAnonKey = process.env.REACT_APP_SUPABASE_ANON_KEY

if (!supabaseUrl || !supabaseAnonKey) {
    // Warn but don't crash, allowing users to fix env
    console.error("Missing Supabase Variables in .env (REACT_APP_SUPABASE_URL, REACT_APP_SUPABASE_ANON_KEY)");
}

export const supabase = createClient(supabaseUrl || "https://placeholder.supabase.co", supabaseAnonKey || "placeholder")
