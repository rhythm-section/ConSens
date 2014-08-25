# Load the Rails application.
require File.expand_path('../application', __FILE__)

# Initialize the Rails application.
Rails.application.initialize!

# Remove ActiveRecord (for MongoDB)
# config.frameworks -= [:active_record]
