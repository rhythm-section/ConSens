module DatabaseConnection

  def get_db_connection
    return @db_connection if @db_connection
    
    db = URI.parse(ENV['MONGOHQ_URL'])
    db_name = db.path.gsub(/^\//, '')

    puts "Database #{db}"

    @db_connection = Moped::Session.new([ "#{db.host}:#{db.port}"])
    @db_connection.use db_name
    @db_connection.login db.user, db.password unless db.user.nil? || db.password.nil?
    @db_connection
  end
  
end