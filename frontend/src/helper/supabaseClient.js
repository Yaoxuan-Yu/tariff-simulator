import { createClient } from '@supabase/supabase-js'

const supabaseUrl = 'https://eommtatuhdmnkakktghz.supabase.co';
const supabaseAnonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVvbW10YXR1aGRtbmtha2t0Z2h6Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc1ODI5NjEzMCwiZXhwIjoyMDczODcyMTMwfQ.c9xhPA5Hfm9aXdfPhn20mzdlNcH3olXaiJ5o8EoydS8";

const supabase = createClient(supabaseUrl, supabaseAnonKey)
export default supabase;