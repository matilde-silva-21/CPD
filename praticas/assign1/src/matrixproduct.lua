local choice

function MatrixMul(lin, col)
    local Time1 = os.clock()
    local matrix_a, matrix_b, matrix_c = {}, {}, {}

    for i = 0, lin - 1 do
        for j = 0, col - 1 do
            matrix_a[i*lin + j] = 1.0
        end
    end

    for i = 0, col - 1 do
       for j = 0, col - 1 do
            matrix_b[i*col + j] = i+1
       end
    end

    for i = 0, col - 1 do
        for j = 0, col - 1 do
             matrix_c[i*col + j] = 0
        end
     end



    for i = 0, lin - 1 do
        for j = 0, col - 1 do
            for k = 0, lin - 1 do
                matrix_c[i * lin + j] = matrix_c[i * lin + j] + matrix_a[i * lin + k] * matrix_b[k * col + j]
            end
        end
    end

    local Time2 = os.clock()
    local st = string.format("Time: %3.3f seconds\n", (Time2 - Time1))

    print(st)
    io.write("Result matrix:\n")
    for i = 0, 0 do
        for j = 0, math.min(10, col) - 1 do
            io.write(matrix_c[j].." ")
        end
    end
    io.write("\n")

end


function LineMatrixMul(lin, col)
    local Time1 = os.clock()
    local matrix_a, matrix_b, matrix_c = {}, {}, {}

    for i = 0, lin - 1 do
        for j = 0, col - 1 do
            matrix_a[i*lin + j] = 1.0
        end
    end

    for i = 0, col - 1 do
       for j = 0, col - 1 do
            matrix_b[i*col + j] = i+1
       end
    end

    for i = 0, col - 1 do
        for j = 0, col - 1 do
             matrix_c[i*col + j] = 0
        end
     end



    for i = 0, lin - 1 do
        for k = 0, col - 1 do
            for j = 0, lin - 1 do
                matrix_c[i * lin + j] = matrix_c[i * lin + j] + matrix_a[i * lin + k] * matrix_b[k * col + j]
            end
        end
    end

    local Time2 = os.clock()
    local st = string.format("Time: %3.3f seconds\n", (Time2 - Time1))

    print(st)
    io.write("Result matrix:\n")
    for _ = 0, 0 do
        for j = 0, math.min(10, col) - 1 do
            io.write(matrix_c[j].." ")
        end
    end
    io.write("\n")
end


function BlockMatrixMul(lin, col, blockSize)
    local Time1 = os.clock()
    local matrix_a, matrix_b, matrix_c = {}, {}, {}

    for i = 0, lin - 1 do
        for j = 0, col - 1 do
            matrix_a[i*lin + j] = 1.0
        end
    end

    for i = 0, col - 1 do
       for j = 0, col - 1 do
            matrix_b[i*col + j] = i+1
       end
    end

    for i = 0, col - 1 do
        for j = 0, col - 1 do
             matrix_c[i*col + j] = 0
        end
     end



    for ii = 0, lin - 1, blockSize do
        for jj = 0, col - 1, blockSize do
            for kk = 0, lin - 1, blockSize do
                for i = ii, ii+blockSize-1, 1 do
                    for j = jj, jj+blockSize-1, 1 do
                        for k = kk, kk+blockSize-1, 1 do
                            matrix_c[i*lin+j] = matrix_c[i*lin+j] + matrix_a[i*lin+k]*matrix_b[k*col+j]
                        end
                    end
                end
            end
        end
    end

    local Time2 = os.clock()
    local st = string.format("Time: %3.3f seconds\n", (Time2 - Time1))

    print(st)
    io.write("Result matrix:\n")
    for i = 0, 0 do
        for j = 0, math.min(10, col) - 1 do
            io.write(matrix_c[j].." ")
        end
    end
    io.write("\n")
end


while choice ~= 0 do
    io.write("1. Multiplication\n")
    io.write("2. Line Multiplication\n")
    io.write("3. Block Multiplication\n")
    io.write("Selection?: ")


    choice = io.read("*n")

    if choice == 0 then break end

    -- io.write("\nDimensions: Line=Cols? ")
    -- local lin = io.read("*n")
    -- local col = lin
    
    local c_tbl =
    {
        [1] = function(x, y) 
            MatrixMul(x, y) 
        end,
        [2] = function(x, y) 
            LineMatrixMul(x, y) 
        end,
        [3] = function(x, y) 
            io.write("Block size? ")
            local blockSize = io.read("*n")
            BlockMatrixMul(x, y, blockSize)
        end 

    }

    local func = c_tbl[choice]
    for i = 600, 3000, 400 do
        if (func) then func(i, i) 
    end
    

    
    end
end

