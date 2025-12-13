const { createClient } = require('@supabase/supabase-js');
require('dotenv').config();

// Cliente con anon key (para operaciones del cliente)
const supabase = createClient(
  process.env.SUPABASE_URL,
  process.env.SUPABASE_ANON_KEY
);

// Cliente con service role (para operaciones admin)
const supabaseAdmin = createClient(
  process.env.SUPABASE_URL,
  process.env.SUPABASE_SERVICE_KEY
);

module.exports = { supabase, supabaseAdmin };